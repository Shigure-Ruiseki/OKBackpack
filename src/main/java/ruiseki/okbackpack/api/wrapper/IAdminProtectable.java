package ruiseki.okbackpack.api.wrapper;

/**
 * Interface for upgrades that can grant admin-level block protection.
 * Blocks with an admin-protected upgrade are immune to explosions and non-creative mining.
 */
public interface IAdminProtectable {

    boolean isAdmin();
}
