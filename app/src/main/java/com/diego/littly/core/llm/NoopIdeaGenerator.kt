package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.GeneratedIdea
import com.diego.littly.core.domain.model.HabitCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class NoopIdeaGenerator @Inject constructor() : IdeaGenerator {

    override fun generate(request: IdeaRequest) = kotlinx.coroutines.flow.flow {
        val pool = banks[request.category].orEmpty()
            .filter { bank -> request.recentIdeas.none { recent -> recent.equals(bank.text, ignoreCase = true) } }
            .ifEmpty { banks.getValue(HabitCategory.GENERIC) }
        val selected = pool.shuffled(Random.Default).take(request.count.coerceAtLeast(1))
        if (selected.isEmpty()) {
            emit(IdeaEvent.Failed("no ideas available"))
        } else {
            emit(IdeaEvent.Completed(selected, MODEL_TAG))
        }
    }

    private val banks: Map<HabitCategory, List<GeneratedIdea>> = mapOf(
        HabitCategory.CREATIVE to listOf(
            GeneratedIdea("Ink garden", "Draw a garden using only swirling ink lines, no shapes allowed."),
            GeneratedIdea("Tiny world", "Sketch a whole city inside an everyday object like a teapot."),
            GeneratedIdea("Color swap", "Draw something familiar but swap two colors in a surprising way."),
            GeneratedIdea("One-line wonder", "Create a drawing without lifting the pen from the paper."),
            GeneratedIdea("Weather report", "Illustrate today's mood as if it were the weather."),
            GeneratedIdea("Blind portrait", "Draw a person or pet without looking at the paper once.")
        ),
        HabitCategory.READING to listOf(
            GeneratedIdea("Genre leap", "Pick a book from a genre you never touch and read the first chapter."),
            GeneratedIdea("Ten-minute essay", "Find one short essay online and read it slowly, twice."),
            GeneratedIdea("Shelf surprise", "Grab a random book from your shelf and read any page that calls to you."),
            GeneratedIdea("Author spotlight", "Read one short story by an author you keep meaning to try."),
            GeneratedIdea("Poetry pause", "Read a single poem out loud and sit with it for a minute."),
            GeneratedIdea("Library wander", "Browse a library or bookstore shelf until one title grabs you.")
        ),
        HabitCategory.LEARNING to listOf(
            GeneratedIdea("Five new words", "Learn five new words in your target language and use each in a sentence."),
            GeneratedIdea("Explain it back", "Study one small concept, then explain it out loud in your own words."),
            GeneratedIdea("Question hunt", "Write down three questions about your topic and answer just one."),
            GeneratedIdea("Speed drill", "Do a quick ten-minute practice drill and track your streak."),
            GeneratedIdea("Tiny translation", "Translate one short sentence and check yourself afterward."),
            GeneratedIdea("Map the topic", "Sketch a mini mind-map of what you already know about your subject.")
        ),
        HabitCategory.FITNESS to listOf(
            GeneratedIdea("Walk and notice", "Take a ten-minute walk and notice three things you usually miss."),
            GeneratedIdea("Stair rounds", "Do a few easy laps of stairs at your own pace."),
            GeneratedIdea("Dance break", "Put on one song and move however feels good."),
            GeneratedIdea("Stretch reset", "Stretch for five minutes, focusing on wherever feels tight."),
            GeneratedIdea("Tiny circuit", "Do ten squats, ten push-ups, and a plank, then smile about it."),
            GeneratedIdea("Fresh air lap", "Step outside and do one easy loop around the block.")
        ),
        HabitCategory.MINDFULNESS to listOf(
            GeneratedIdea("Four-count breath", "Breathe in for four, out for six, ten times, eyes closed."),
            GeneratedIdea("Three good things", "Write down three small things that went right today."),
            GeneratedIdea("Sound scan", "Close your eyes and name five different sounds you can hear."),
            GeneratedIdea("Slow sip", "Drink something warm slowly, noticing every part of it."),
            GeneratedIdea("Body check", "Scan from head to toes and gently relax wherever you find tension."),
            GeneratedIdea("Kind note", "Write a one-line kind message to your future self.")
        ),
        HabitCategory.SOCIAL to listOf(
            GeneratedIdea("Two-line hello", "Send a short, warm message to someone you have not seen in a while."),
            GeneratedIdea("Voice note", "Record a cheerful voice note instead of a text."),
            GeneratedIdea("Gratitude ping", "Tell someone one specific thing you appreciate about them."),
            GeneratedIdea("Plan a moment", "Suggest a simple coffee or walk date to a friend."),
            GeneratedIdea("Comment kindly", "Leave a thoughtful comment on something a friend shared."),
            GeneratedIdea("Family check-in", "Call a family member for a quick five-minute catch-up.")
        ),
        HabitCategory.PRODUCTIVITY to listOf(
            GeneratedIdea("Two-minute sweep", "Clear one small surface or folder until it feels calm."),
            GeneratedIdea("Inbox five", "Answer or archive just five emails, then stop."),
            GeneratedIdea("Tomorrow's three", "Write the three things that would make tomorrow a win."),
            GeneratedIdea("One-drawer tidy", "Organize a single drawer and enjoy the order."),
            GeneratedIdea("Money minute", "Glance at one account and note anything that needs attention."),
            GeneratedIdea("Plan the week", "Jot one intention for each day of the coming week.")
        ),
        HabitCategory.GENERIC to listOf(
            GeneratedIdea("Five-minute start", "Set a timer and give the habit just five honest minutes."),
            GeneratedIdea("Tiny milestone", "Pick the smallest possible win and bank it today."),
            GeneratedIdea("Show and tell", "Share one line about your progress with someone who cheers you on."),
            GeneratedIdea("Change the scene", "Do today's session somewhere you have never done it before."),
            GeneratedIdea("Pair it up", "Attach the habit to something you already do every day."),
            GeneratedIdea("Look back", "Spend a minute noticing how far you have already come.")
        )
    )

    private companion object {
        const val MODEL_TAG = "curated"
    }
}
