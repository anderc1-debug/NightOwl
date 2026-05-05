package com.nightowl;

import java.util.HashMap;
import java.util.Map;

public class CampusResources {

    public record Campus(
        String name,
        // Disability / Accessibility Services
        String disabilityServicesName,
        String disabilityServicesLocation,
        String disabilityServicesPhone,
        String disabilityServicesHours,
        String disabilityServices,
        String disabilityEmail,
        String testingEmail,
        // Counseling / Mental Health
        String counselingName,
        String counselingPhone,
        String counseling,
        String mentalHealth,
        // Health & Wellness
        String healthName,
        String healthPhone,
        String healthAndWellness,
        String timelyCare,
        // Emergency
        String policePhone,
        String emergencyAlerts,
        String activeShooter,
        String evacuation,
        // Map & Shuttle
        String shuttleTracker,
        String campusMap,
        // Library
        String libraryName,
        String libraryPhone,
        String libraryHours,
        String studyRoomBooking
    ) {}

    private static final Map<String, Campus> CAMPUSES = new HashMap<>();

    static {
        CAMPUSES.put("Farmingdale State College", new Campus(
            "Farmingdale State College",
            "Disability Services Center",
            "Whitman Hall, Room 186D  |  Testing Center: Room 183",
            "934-420-5174",
            "Monday - Friday, 8:30am - 4:00pm",
            "https://www.farmingdale.edu/disability-services-center/",
            "DSC@farmingdale.edu",
            "TESTING@farmingdale.edu",
            "Counseling and Psychological Services",
            "934-420-2006",
            "https://www.farmingdale.edu/campus-mental-health-services/",
            "https://www.farmingdale.edu/campus-mental-health-services/",
            "Health and Wellness Center",
            "934-420-2556",
            "https://www.farmingdale.edu/health-wellness-center/",
            "https://timelycare.com/",
            "934-420-5765",
            "https://www.farmingdale.edu/university-police/emergency-alerts.shtml",
            "https://www.farmingdale.edu/university-police/active-shooter-preparedness.shtml",
            "https://www.farmingdale.edu/university-police/pdf/evacuation_guidelines.pdf",
            "https://farmingdale.downtownerapp.com/routes/",
            "https://map.farmingdale.edu/",
            "Greenley Library",
            "934-420-2524",
            "https://www.farmingdale.edu/library/hours.shtml",
            "https://farmingdale.libcal.com/spaces"
        ));

        CAMPUSES.put("Stony Brook University", new Campus(
            "Stony Brook University",
            "Student Accessibility Support Center",
            "Educational Communications Center (ECC), Room 116",
            "631-632-6748",
            "Monday - Friday, 8:30am - 5:00pm",
            "https://www.stonybrook.edu/dss/",
            "studentabilityservices@stonybrook.edu",
            "studentabilityservices@stonybrook.edu",
            "Counseling and Psychological Services (CAPS)",
            "631-632-6720",
            "https://www.stonybrook.edu/caps/",
            "https://www.stonybrook.edu/caps/",
            "Student Health Services",
            "631-632-6740",
            "https://www.stonybrook.edu/health/",
            "https://timelycare.com/",
            "631-632-6350",
            "https://www.stonybrook.edu/police/",
            "https://www.stonybrook.edu/police/safety/emergency-preparedness/",
            "https://www.stonybrook.edu/ehs/fire-safety/",
            "https://www.stonybrook.edu/parking/",
            "https://www.stonybrook.edu/maps/",
            "Frank Melville Jr. Memorial Library",
            "631-632-7100",
            "https://library.stonybrook.edu/hours/",
            "https://library.stonybrook.edu/spaces/"
        ));

        CAMPUSES.put("SUNY Old Westbury", new Campus(
            "SUNY Old Westbury",
            "Accessibility Resources",
            "Campus Center, Room I-211",
            "516-876-3135",
            "Monday - Friday, 9:00am - 5:00pm",
            "https://www.oldwestbury.edu/division/office-student-affairs/office-services-students-disabilities/",
            "disability@oldwestbury.edu",
            "disability@oldwestbury.edu",
            "Counseling and Psychological Wellness Services",
            "516-876-3053",
            "https://www.oldwestbury.edu/division/office-student-affairs/counseling-psychological-wellness-services/",
            "https://www.oldwestbury.edu/division/office-student-affairs/counseling-psychological-wellness-services/",
            "Student Health Center",
            "516-876-3183",
            "https://www.oldwestbury.edu/division/office-student-affairs/student-health-center/",
            "https://timelycare.com/",
            "516-876-3333",
            "https://www.oldwestbury.edu/university-police/",
            "https://www.oldwestbury.edu/university-police/",
            "https://www.oldwestbury.edu/university-police/",
            "https://www.oldwestbury.edu/division/division-business-finance/transportation-services/",
            "https://www.oldwestbury.edu/why-old-westbury/visit-old-westbury/campus-map/",
            "SUNY Old Westbury Library",
            "516-876-3156",
            "https://www.oldwestbury.edu/division/office-academic-affairs/library/",
            "https://oldwestbury.libcal.com/spaces"
        ));

        CAMPUSES.put("Nassau Community College", new Campus(
            "Nassau Community College",
            "Center for Students with Disabilities",
            "Building B, Room 107",
            "516-572-7241",
            "Monday - Friday, 9:00am - 5:00pm",
            "https://www.ncc.edu/campusservices/disabilities_services/",
            "disabilityservices@ncc.edu",
            "disabilityservices@ncc.edu",
            "Personal Counseling Center",
            "516-572-7134",
            "https://www.ncc.edu/campusservices/counselingservices/",
            "https://www.ncc.edu/campusservices/counselingservices/",
            "Health Services",
            "516-572-7425",
            "https://www.ncc.edu/campusservices/health_services/",
            "https://timelycare.com/",
            "516-572-7777",
            "https://www.ncc.edu/campusservices/parkingandsafety/",
            "https://www.ncc.edu/campusservices/parkingandsafety/",
            "https://www.ncc.edu/campusservices/parkingandsafety/",
            "https://www.ncc.edu/campusservices/parkingandsafety/mapanddirections.shtml",
            "https://www.ncc.edu/campusservices/parkingandsafety/pdfs/NCCmap.pdf",
            "NCC Library",
            "516-572-7349",
            "https://library.ncc.edu/libraryhours",
            "https://ncc.libcal.com/spaces"
        ));

        CAMPUSES.put("Hofstra University", new Campus(
            "Hofstra University",
            "Student Access Services",
            "241 Student Center",
            "516-463-7075",
            "Monday - Friday, 8:30am - 5:00pm",
            "https://www.hofstra.edu/student-access-services/",
            "access@hofstra.edu",
            "access@hofstra.edu",
            "Counseling and Psychological Services",
            "516-463-6791",
            "https://www.hofstra.edu/student-counseling-services/",
            "https://www.hofstra.edu/student-counseling-services/",
            "Student Health Services",
            "516-463-6745",
            "https://www.hofstra.edu/student-health/",
            "https://timelycare.com/",
            "516-463-6606",
            "https://www.hofstra.edu/campus-life/public-safety/",
            "https://www.hofstra.edu/campus-life/public-safety/",
            "https://www.hofstra.edu/campus-life/public-safety/",
            "https://www.hofstra.edu/campus-life/public-safety/transportation/",
            "https://www.hofstra.edu/visitors/directions-maps.html",
            "Axinn Library",
            "516-463-5952",
            "https://libcal.hofstra.edu/hours/",
            "https://libcal.hofstra.edu/spaces"
        ));

        CAMPUSES.put("Adelphi University", new Campus(
            "Adelphi University",
            "Student Access Office",
            "Nexus Building, Room 201",
            "516-877-3806",
            "Monday - Friday, 8:30am - 4:30pm",
            "https://www.adelphi.edu/access-office/",
            "sao@adelphi.edu",
            "sao@adelphi.edu",
            "Center for Psychological Services",
            "516-877-3646",
            "https://www.adelphi.edu/scc/",
            "https://www.adelphi.edu/scc/",
            "Student Health Services",
            "516-877-3230",
            "https://www.adelphi.edu/student-life/health-wellness-safety/",
            "https://timelycare.com/",
            "516-877-3511",
            "https://www.adelphi.edu/safety-transportation/",
            "https://www.adelphi.edu/safety-transportation/",
            "https://www.adelphi.edu/safety-transportation/",
            "https://www.adelphi.edu/safety-transportation/transportation-and-parking/",
            "https://www.adelphi.edu/maps-directions/",
            "Swirbul Library",
            "516-877-3572",
            "https://www.adelphi.edu/libraries/using-the-libraries/library-hours/",
            "https://collabstudios.adelphi.edu/reserve/studios"
        ));

        CAMPUSES.put("Other", new Campus(
            "Your Campus",
            "Accessibility / Disability Services",
            "Contact your campus for location",
            "Contact your campus",
            "Contact your campus for hours",
            "https://www.google.com/search?q=disability+services+my+college",
            "",
            "",
            "Counseling Services",
            "Contact your campus",
            "https://www.google.com/search?q=counseling+services+my+college",
            "https://www.google.com/search?q=mental+health+services+my+college",
            "Health Services",
            "Contact your campus",
            "https://www.google.com/search?q=health+wellness+my+college",
            "https://timelycare.com/",
            "911",
            "https://www.google.com/search?q=emergency+alerts+my+college",
            "https://www.google.com/search?q=active+shooter+preparedness",
            "https://www.google.com/search?q=evacuation+procedures+my+college",
            "https://www.google.com/search?q=shuttle+tracker+my+college",
            "https://www.google.com/search?q=campus+map+my+college",
            "Campus Library",
            "Contact your campus",
            "https://www.google.com/search?q=library+hours+my+college",
            "https://www.google.com/search?q=study+room+booking+my+college"
        ));
    }

    public static Campus get(String schoolName) {
        return CAMPUSES.getOrDefault(schoolName, CAMPUSES.get("Other"));
    }
}
