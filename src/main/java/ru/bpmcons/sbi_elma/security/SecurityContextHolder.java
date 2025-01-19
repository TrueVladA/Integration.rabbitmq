package ru.bpmcons.sbi_elma.security;

import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.keycloak.KeycloakJwtInfo;

import javax.validation.constraints.NotNull;

@UtilityClass
public class SecurityContextHolder {
    private static final ThreadLocal<SecurityContext> ctx = ThreadLocal.withInitial(() -> null);

    @Nullable
    public static SecurityContext getContext() {
        return ctx.get();
    }

    @NonNull
    public static SecurityContext getRequiredContext() {
        SecurityContext context = ctx.get();
        if (context == null) {
            throw new IllegalStateException("Security context not initialized");
        }
        return context;
    }

    @NotNull
    public static KeycloakJwtInfo getRequiredPrincipal() {
        KeycloakJwtInfo principal = getRequiredContext().getPrincipal();
        if (principal == null) {
            throw new IllegalStateException("Principal not initialized");
        }
        return principal;
    }

    @Nullable
    public static String getPrincipalName() {
        KeycloakJwtInfo principal = getRequiredContext().getPrincipal();
        if (principal == null) {
            return null;
        }
        return principal.getName();
    }

    public static void setContext(@NonNull SecurityContext context) {
        ctx.set(context);
    }

    public static void setContextNullable(@Nullable SecurityContext context) {
        ctx.set(context);
    }

    public static void setPrincipal(@NonNull KeycloakJwtInfo info) {
        SecurityContext context = ctx.get();
        if (context == null) {
            throw new IllegalStateException("Security context not initialized");
        }
        ctx.set(new SecurityContext(context.getSystem(), info));
    }

    public static void resetContext() {
        ctx.remove();
    }
}
