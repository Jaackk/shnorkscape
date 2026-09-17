package com.rs.utils;

public final class Colors {

	public static final String RED = "<col=ff0000>";
	public static final String SALMON = "<col=ff3333>";
	public static final String GREEN = "<col=00ff00>";
	public static final String DARK_GREEN = "<col=25a832>";
	public static final String LIME = "<col=32cd32>";
	public static final String BLUE = "<col=0000ff>";
	public static final String WHITE = "<col=ffffff>";
	public static final String BLACK = "<col=000000>";
	public static final String ORANGE = "<col=f2490c>";
	public static final String CYAN = "<col=00B2ED>";
	public static final String RCYAN = "<col=80ffff>";
	public static final String PBLUE = "<col=0867af>";
	public static final String CMT = "<col=00ffff>";
	public static final String PINK = "<col=efa0be>";
	public static final String DCYAN = "<col=00b3b3>";
	public static final String GRAY = "<col=A6A6A6>";
	public static final String LIGHT_GRAY = "<col=C4C4C4>";
	public static final String DARK_RED = "<col=B52100>";
	public static final String YELLOW = "<col=FFFF00>";
	public static final String BROWN = "<col=8C6C0D>";
	public static final String GOLD = "<col=E6CC07>";
	public static final String LPURPLE = "<col=a942b7>";
	public static final String DPURPLE = "<col=5e006b>";
	public static final String DIAMOND = "<col=00e1ff>";
	public static final String DEF_SEARCH_CYAN = "<col=00FFFF>";

	public static final String SHAD = "<shad=000000>";
	public static final String ESHAD = "<shad=ffffff>";

	public static final String check(boolean finished) {
		return finished ? GREEN : RED;
	}

	public static final String shade(String color) {
		return SHAD + color;
	}

	public static final String wrap(String color, String text) {
		return color + text + (color.contains("<shad=") ? "</col></shad>" : "</col>");
	}
}
