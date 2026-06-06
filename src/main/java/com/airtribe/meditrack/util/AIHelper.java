package com.airtribe.meditrack.util;

import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.Specialization;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rule-based AI helper for doctor recommendation and slot suggestion.
 * Demonstrates: streams, lambdas, rule-based logic (Bonus C).
 */
public final class AIHelper {

    private AIHelper() {}

    /**
     * Recommends doctors based on symptom keywords.
     * Uses rule-based matching defined in Doctor.matchesSymptoms().
     */
    public static List<Doctor> recommendDoctors(List<Doctor> allDoctors, String symptoms) {
        return allDoctors.stream()
                .filter(d -> d.isAvailable() && d.matchesSymptoms(symptoms))
                .sorted((a, b) -> Double.compare(a.getConsultationFee(), b.getConsultationFee()))
                .collect(Collectors.toList());
    }

    /**
     * Suggests next available appointment slots.
     * @param count how many slots to suggest
     */
    public static List<String> suggestSlots(int count) {
        return DateUtil.suggestSlots(count);
    }

    /**
     * Returns the specialization most relevant to a symptom string.
     */
    public static Specialization guessSpecialization(String symptoms) {
        String s = symptoms.toLowerCase();
        if (s.contains("chest") || s.contains("heart"))   return Specialization.CARDIOLOGIST;
        if (s.contains("skin") || s.contains("rash"))     return Specialization.DERMATOLOGIST;
        if (s.contains("headache") || s.contains("brain")) return Specialization.NEUROLOGIST;
        if (s.contains("bone") || s.contains("joint"))    return Specialization.ORTHOPEDIST;
        if (s.contains("child") || s.contains("fever"))   return Specialization.PEDIATRICIAN;
        if (s.contains("anxiety") || s.contains("depression")) return Specialization.PSYCHIATRIST;
        if (s.contains("eye") || s.contains("vision"))    return Specialization.OPHTHALMOLOGIST;
        if (s.contains("ear") || s.contains("throat"))    return Specialization.ENT_SPECIALIST;
        if (s.contains("pregnancy") || s.contains("menstrual")) return Specialization.GYNECOLOGIST;
        return Specialization.GENERAL_PHYSICIAN;
    }

    public static void printRecommendations(List<Doctor> doctors, String symptoms) {
        System.out.println("\n--- AI Doctor Recommendations for: \"" + symptoms + "\" ---");
        List<Doctor> recommended = recommendDoctors(doctors, symptoms);
        if (recommended.isEmpty()) {
            System.out.println("  No specific match found. Consider a General Physician.");
        } else {
            recommended.forEach(d -> System.out.println("  " + d.getDescription()));
        }
        System.out.println("\n  Suggested slots:");
        suggestSlots(3).forEach(s -> System.out.println("  -> " + s));
    }
}
