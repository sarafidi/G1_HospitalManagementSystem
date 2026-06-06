package util;

// called by view panels before passing anything to controller
// protects the system from bad inputs
public class Validator {
    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return isNonEmpty(email) && email.matches("^[^ \\s@]+@[^ \\s@]*\\.\\w+$");
    }

    public static boolean isValidPhone(String phone) {
        return isNonEmpty(phone) && phone.matches("^0\\d{9,10}$");
    }

    public static boolean isValidAge(int age) {
        return age >= 0 && age <= 125;
    }

    public static boolean passwordsMatch(String p1, String p2) {
        return p1.equals(p2);
    }
}