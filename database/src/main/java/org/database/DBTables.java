package org.database;

public class DBTables {

    public class TNote {
        public static final String TABLE_NAME = "NOTE";
        public static final String _ID_NOTE = "_ID_NOTE";
        public static final String _ID_SONG = "_ID_SONG";
        public static final String _ID_CATEGORY = "_ID_CATEGORY";
        public static final String _TOP = "_TOP";
        public static final String _LEFT = "_LEFT";
        public static final String _RIGHT = "_RIGHT";
        public static final String _BOTTOM = "_BOTTOM";
        public static final String _SELECTED = "_SELECTED";
    }

    public class TSong {
        public static final String TABLE_NAME = "SONG";
        public static final String _ID = "_ID_SONG";
        public static final String _NAME = "_NAME";
        public static final String _AUTOR = "_AUTOR";
        public static final String _CLEF = "_CLEF";
        public static final String _TIME = "_TIME";
    }

    public class TCategory {
        public static final String TABLE_NAME = "CATEGORY";
        public static final String _ID_CATEGORY = "_ID_CATEGORY";
        public static final String _IMAGE = "_IMAGE";
    }

    public class TFavs {
        public static final String TABLE_NAME = "FAVS";
    }
}
