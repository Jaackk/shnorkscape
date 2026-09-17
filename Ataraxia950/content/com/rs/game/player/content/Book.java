package com.rs.game.player.content;

public enum Book {

	THE_PRAESUL(39595, "The Praesul", "My palace. How strange to return here after so long. How stranger still that to me it has been an eternity of centuries, but in truth I had lain entombed in ice for thousands of years longer. Demons and mahjarrat speak of the passage of millennia with weary disinterest, but I am no such ancient creature. How strange also to have spent so long trapped in a temple built unknowingly atop this place, held merely a score of places from my sanctum. But no matter. ", 
			 "The rebellion and war are ancient history now, as recent as they still seems to me. I built this place, here, deep underground, at a convergence of the dark elements on this world. My lord promised me a source of power, and I prepared this place to house it, but thanks to the traitor Zamorak it was never delivered. And how strange to walk the floors of this place without my companions. My blood reavers, slain by the fools who released me from my prison. My legions, ", 
			"defeated long ago by the Saradominists. And of course, my tribunes: Torva, Pernix and Virtus. Before them, I was a weapon. Before them, I was nothing but a weapon. They taught me of war, yes, which was why I recruited them, but they taught me of so much more. Torva, who was mighty in battle, had also proven himself a leader of men. Though like him I feared nothing, Torva turned his courage into inspiration and found a way to share it with those around him.", 
			"Humans, unlike demons, scale their performance dramatically with their psychological state, and it was Torva who showed me how to manipulate that. Pernixs incredible powers of perception were not limited to the battlefield. Past the blankness of his mask, he was always watching. The slight tear on the hem of a pontifexs robe. The sweat on the brow of a supposedly relaxed soldier. From Pernix, of all people, I learned to read social cues and thus began to",
			"be able to interact with society. And Virtus. From the nihil I learned only of annihilation, and my Lord was well placed with my performance in battle, but Virtus believed in more than slaughter. Though his concern was the protection of our allies through magic, he caused me to widen my perception of what a warrior - and ultimate, even a living being - could be. Though the first thing I learned from him was to defend, that jump from one approach to two caused me to",
			"question: what more could there be? And so the world of strategy was opened to me, and from that all things became possible. But alas, my tribunes are long gone. Only one remains from that time, beyond the petty and scheming mahjarrat: Char, the dancer. Does she still pine hopelessly for our Lord? Through Pernixs eyes her feelings were always clear. Since her return, she has had no true home, being forced to abode with Azzanadra whom I know she dislikes.",
			"Perhaps I will invite her here, and together we can recreate a tiny part of the grandeur of the empire."),
	THE_PROMISED_GIFT(39594, "The Promised Gift", "He returned to me then. Zaros. My Lord. My creator. His words were these: I had always seen you as a failure, Nex. Between the time of your creation and my banishment I spared little thought to you beyond what small use you could provide to me as a tool. In that, I was wrong. The failure was mine. I created you and your kind to fulfil a purpose, to demonstrate my mastery over anima and the creation of life. That you failed to fulfil that purpose",
			"is no responsibility or fault of yours. In every other area I could ask for, you have exceeded expectation. Your role is to watch over the younger gods, but even you cannot stand against them directly now that the edicts have fallen. To that end I present you with a gift - a gift I promised you millennia ago but have been unable to fulfil: the four power stones that were used to create your kind. They are the pure embodiment of the elemental forces",
			"of the lower worlds: smoke, shadow, ice and blood. I created the stones as a pure focus for the dark elements to aid in my work creating the nihil, but I no longer need them for that purpose and I now have access to more sophisticated tools. For you, especially because of your heritage, they should prove extremely useful. The strength you can draw from them will empower you for your tasks to come. Each of the stones was formed from the essence",
			"of a powerful inhabitant of the lower worlds. I created the stone of smoke from the remains of an especially large smoke demon from Infernus. Smoke demons are amongst the most inscrutable of demons, close in nature and in location to the heart of their world, and a powerful embodiment of that element. I created the stone of shadow from the essence of one thousand and one shadow cacklers from the shadow realm. Though individually weak, they are powerful",
			"in large concentration, but far less risky than hunting down one of the true leviathans that lurk in the deep places of that realm. The stone of blood was once the bloody matriarch of Vampyrium. This trueborn vampyre was the mother of my once-servant Drakan, and during my time on Gielinor had fallen from my loyal service and grown corpulent in the manner of ancient Hostilius. Finally I created the stone of ice from an arch-glacor on the frozen world of Leng. These creatures,",
			"that tower ten or a hundred times taller than their smaller kin, dominate the landscape of that brutal and inhospitable wasteland. I must go now to confront Sliske. I will take Azzanadra, Char, and Vindicta with me, but in my absence I need you to remain and keep watch for treachery on the part of my sister or any of the lesser gods. And then he left, and once he was gone, I wept for his acceptance of me.")
	
	;
	
	private final int itemId;
	private final String title;
	private final String[] pages;

	Book(int itemId, String title, String... pages) {
		this.itemId = itemId;
		this.title = title;
		this.pages = pages;
	}

	public int getItemId() {
		return itemId;
	}

	public String getTitle() {
		return title;
	}

	public String[] getPages() {
		return pages;
	}
}
