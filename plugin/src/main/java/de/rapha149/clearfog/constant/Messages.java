package de.rapha149.clearfog.constant;

public final class Messages {

    public static final String PREFIX = "<dark_gray>[<dark_red>ClearFog</dark_red>]</dark_gray> ";
    public static final String NO_PERMISSION = PREFIX + "<red>You do not have permission to execute this command.</red>";
    public static final String RELOAD_SUCCESS = PREFIX + "<gray>Configuration and messages successfully reloaded.</gray>";
    public static final String INVALID_NUMBER = PREFIX + "<red>Please specify a valid positive number.</red>";
    public static final String VALUE_OUT_OF_BOUNDS = PREFIX + "<red>View distance must be greater than or equal to 1.</red>";
    public static final String PLAYER_NOT_FOUND = PREFIX + "<red>The player <gray><player></gray> could not be found.</red>";
    public static final String WORLD_NOT_FOUND = PREFIX + "<red>The world <gray><world></gray> could not be found.</red>";
    public static final String FEATURE_DISABLED = PREFIX + "<red>This feature is currently disabled in the configuration.</red>";
    
    public static final String STATUS_ENABLED = PREFIX + "<gray><feature> is currently <green>enabled</green>.</gray>";
    public static final String STATUS_DISABLED = PREFIX + "<gray><feature> is currently <red>disabled</red>.</gray>";
    public static final String STATUS_ALREADY_SET = PREFIX + "<gold><feature> is already set to this state.</gold>";
    public static final String STATUS_CHANGE_SUCCESS = PREFIX + "<gray><feature> is now <state>.</gray>";

    public static final String VALUE_CURRENT = PREFIX + "<gray><target> view distance is currently <gold><distance></gold>.</gray>";
    public static final String VALUE_ALREADY_SET = PREFIX + "<gold><target> view distance is already set to <gray><distance></gray>.</gold>";
    public static final String VALUE_SET_SUCCESS = PREFIX + "<gray><target> view distance was set to <gold><distance></gold>.</gray>";
    public static final String VALUE_UNSET_SUCCESS = PREFIX + "<gray><target> view distance override has been removed.</gray>";
    public static final String VALUE_NOT_SET = PREFIX + "<gray><target> view distance is <gold>not set</gold>.</gray>";

    public static final String LIST_HEADER = PREFIX + "<gray><target> view distances:</gray>";
    public static final String LIST_ENTRY = "<gray>- <gold><name></gold>: <yellow><distance></yellow></gray>";
    public static final String LIST_EMPTY = PREFIX + "<gray>No overrides are currently configured.</gray>";

    private Messages() {
    }
}
