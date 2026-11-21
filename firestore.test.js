const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "parent_alice_1";
const BOB_UID = "parent_bob_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function buildSampleProfile(uid, ts) {
  return {
    userId: uid,
    preferredName: "Barsha",
    age: 5,
    school: "Saraswati Shishu Mandir",
    studentClass: "UKG",
    rollNumber: "07",
    board: "CBSE",
    schoolMedium: "Odia",
    preferredLanguage: "ODIA",
    subjects: ["English", "Hindi", "Odia", "Mathematics"],
    subjectDurations: { English: 20, Mathematics: 20 },
    interests: ["Diyas", "Festivals"],
    avatarColorHex: "#D9531E",
    sampleProfile: true,
    cameraPermissionForChild: false,
    lastLessonId: "ukg_three_festival_lamps",
    lastLessonTitle: "The Three Festival Lamps",
    lastConceptId: "counting_one_to_one",
    lastStepIndex: 0,
    lastTeachingApproach: "story_scene",
    createdAt: ts,
    updatedAt: ts,
  };
}

test("Unauthenticated user cannot read or write profiles or settings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("profiles").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("profiles").doc("barsha").set(buildSampleProfile(ALICE_UID, ts))
  );
});

test("Owner can create, update, list, and delete student profiles and settings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const profileRef = aliceDb.collection("users").doc(ALICE_UID).collection("profiles").doc("barsha_ukg");

  await assertSucceeds(profileRef.set(buildSampleProfile(ALICE_UID, created)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("profiles").get());

  // Partial update preserving createdAt and userId
  await assertSucceeds(
    profileRef.update({
      preferredLanguage: "HINDI",
      lastStepIndex: 3,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field / @DocumentId 'id' injection
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("profiles").doc("raghav_c4").set({
      ...buildSampleProfile(ALICE_UID, created),
      id: "raghav_c4",
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    profileRef.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("profiles").doc("future_child").set(
      buildSampleProfile(ALICE_UID, new Date(Date.now() + 60000))
    )
  );
});

test("Cross-user isolation blocks Bob from reading or updating Alice's data", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore()
      .collection("users").doc(ALICE_UID).collection("profiles").doc("barsha_ukg")
      .set(buildSampleProfile(ALICE_UID, created));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("profiles").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("profiles").doc("barsha_ukg").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("profiles").doc("barsha_ukg").update({
      preferredName: "Hacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("ConceptMastery, LearningHistory, WeeklyTests (terminal lock), and Settings rules work", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  // Progress
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("progress").doc("p1").set({
      userId: ALICE_UID,
      profileId: "barsha_ukg",
      conceptId: "counting_one_to_one",
      conceptTitle: "One-to-One Lamp Counting",
      subject: "Mathematics",
      lessonId: "ukg_three_festival_lamps",
      independentCorrect: 3,
      hintAssistedCorrect: 1,
      attempts: 4,
      misconceptions: [],
      needsPractice: false,
      nextDayRecallDue: true,
      nextDayRecallPassed: false,
      lastMethodUsed: "tap_each_lamp_with_note",
      demonstratedEvidence: "3 of 4 answered independently",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Weekly test + terminal completed lock
  const testRef = aliceDb.collection("users").doc(ALICE_UID).collection("weeklyTests").doc("t1");
  await assertSucceeds(
    testRef.set({
      userId: ALICE_UID,
      profileId: "raghav_c4",
      testDate: "2026-11-22",
      subject: "Science",
      totalQuestions: 3,
      correctCount: 2,
      currentQuestionIndex: 2,
      completed: false,
      conceptsNeedingRevision: ["evaporation_vs_absorption"],
      answersSummary: ["Q1: Correct", "Q2: Needs hint"],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    testRef.update({
      correctCount: 3,
      currentQuestionIndex: 3,
      completed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Once completed == true, subsequent updates must be rejected
  await assertFails(
    testRef.update({
      correctCount: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Voice settings
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("settings").doc("voice_and_app").set({
      userId: ALICE_UID,
      selectedLanguage: "HINDI",
      selectedStoryStyle: "HUMAN_ADVENTURE",
      speechSpeed: 1.0,
      narrationVolume: 0.9,
      musicVolume: 0.35,
      captionsEnabled: true,
      lightweightMode: false,
      highResolutionMode: true,
      indianEpicModeEnabled: true,
      offlinePackDownloaded: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});
