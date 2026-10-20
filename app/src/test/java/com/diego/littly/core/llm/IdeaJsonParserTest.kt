package com.diego.littly.core.llm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdeaJsonParserTest {

    @Test
    fun parsesValidJson() {
        val raw = """{"ideas":[{"title":"Ink garden","idea":"Draw a garden with ink swirls."}]}"""
        val ideas = IdeaJsonParser.parse(raw)

        assertEquals(1, ideas.size)
        assertEquals("Ink garden", ideas[0].title)
        assertEquals("Draw a garden with ink swirls.", ideas[0].text)
    }

    @Test
    fun parsesMultipleIdeas() {
        val raw = """
            {"ideas":[
              {"title":"One","idea":"First idea."},
              {"title":"Two","idea":"Second idea."},
              {"title":"Three","idea":"Third idea."}
            ]}
        """.trimIndent()
        assertEquals(3, IdeaJsonParser.parse(raw).size)
    }

    @Test
    fun stripsCodeFences() {
        val raw = """
            ```json
            {"ideas":[{"title":"Fenced","idea":"Came from a fenced block."}]}
            ```
        """.trimIndent()
        val ideas = IdeaJsonParser.parse(raw)
        assertEquals("Came from a fenced block.", ideas[0].text)
    }

    @Test
    fun recoversFromSurroundingProse() {
        val raw = """
            Sure! Here are your ideas:
            {"ideas":[{"title":"Prose wrapped","idea":"Hidden inside chatter."}]}
            Hope these help!
        """.trimIndent()
        val ideas = IdeaJsonParser.parse(raw)
        assertEquals("Hidden inside chatter.", ideas[0].text)
    }

    @Test
    fun recoversFromTruncatedJsonWithSalvage() {
        val raw = """
            {"ideas":[
              {"title":"Kept","idea":"Salvaged from broken json."},
              {"title":"Lost","idea":"This one gets cut of
        """.trimIndent()
        val ideas = IdeaJsonParser.parse(raw)
        assertEquals(1, ideas.size)
        assertEquals("Salvaged from broken json.", ideas[0].text)
    }

    @Test
    fun salvageHandlesMissingTitles() {
        val raw = """blah "idea": "No title anywhere." more blah "idea": "Second body." end"""
        val ideas = IdeaJsonParser.parse(raw)

        assertEquals(2, ideas.size)
        assertNull(ideas[0].title)
        assertEquals("No title anywhere.", ideas[0].text)
        assertEquals("Second body.", ideas[1].text)
    }

    @Test
    fun acceptsTextFieldInsteadOfIdea() {
        val raw = """{"ideas":[{"title":"T","text":"Text field body."}]}"""
        val ideas = IdeaJsonParser.parse(raw)
        assertEquals("Text field body.", ideas[0].text)
    }

    @Test
    fun capsIdeaCount() {
        val raw = """
            {"ideas":[
              {"title":"1","idea":"A."},
              {"title":"2","idea":"B."},
              {"title":"3","idea":"C."},
              {"title":"4","idea":"D."}
            ]}
        """.trimIndent()
        assertEquals(3, IdeaJsonParser.parse(raw, maxIdeas = 3).size)
    }

    @Test
    fun returnsEmptyForGarbage() {
        assertTrue(IdeaJsonParser.parse("The model just rambled about weather.").isEmpty())
    }

    @Test
    fun returnsEmptyForBlankInput() {
        assertTrue(IdeaJsonParser.parse("   ").isEmpty())
    }

    @Test
    fun cleansEscapedNewlines() {
        val raw = """{"ideas":[{"title":"T","idea":"Line one.\nLine two."}]}"""
        val ideas = IdeaJsonParser.parse(raw)
        assertEquals("Line one. Line two.", ideas[0].text)
    }

    @Test
    fun ignoresIdeasWithBlankBodies() {
        val raw = """
            {"ideas":[
              {"title":"Empty","idea":""},
              {"title":"Real","idea":"Has content."}
            ]}
        """.trimIndent()
        val ideas = IdeaJsonParser.parse(raw)
        assertEquals(1, ideas.size)
        assertEquals("Has content.", ideas[0].text)
    }
}
