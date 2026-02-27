package tn.esprit.educlass.utlis;

public class ValidationUtils {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String NAME_REGEX = "^[A-Za-zÀ-ÿ\\s\\-]{2,50}$";

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Le mot de passe doit contenir au moins une majuscule.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Le mot de passe doit contenir au moins un chiffre.";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*")) {
            return "Le mot de passe doit contenir au moins un caractère spécial.";
        }
        return null; // valid
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches(NAME_REGEX);
    }

    public static String validateName(String name, String fieldLabel) {
        if (name == null || name.trim().isEmpty()) {
            return fieldLabel + " est requis.";
        }
        if (name.trim().length() < 2) {
            return fieldLabel + " doit contenir au moins 2 caractères.";
        }
        if (name.trim().length() > 50) {
            return fieldLabel + " ne doit pas dépasser 50 caractères.";
        }
        if (!name.trim().matches(NAME_REGEX)) {
            return fieldLabel + " ne peut contenir que des lettres, espaces et tirets.";
        }
        return null; // valid
    }
}

