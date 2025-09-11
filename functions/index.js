const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Trigger: When a new check-in document is created under an appointment.
 */
exports.updateAppointmentStatusOnCheckIn = functions.firestore
  .document("appointments/{appointmentId}/checkin/{userId}")
  .onCreate(async (snapshot, context) => {
    const { appointmentId } = context.params;

    try {
      // Update the appointment’s status to "Checked-In"
      await admin.firestore()
        .collection("appointments")
        .doc(appointmentId)
        .update({
          status: "Checked-In",
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

      console.log(`✅ Appointment ${appointmentId} updated to Checked-In`);
    } catch (error) {
      console.error("Error updating appointment:", error);
    }
  });
