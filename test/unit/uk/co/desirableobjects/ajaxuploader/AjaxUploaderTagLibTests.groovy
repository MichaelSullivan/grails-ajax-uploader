package uk.co.desirableobjects.ajaxuploader

import grails.test.*
import org.junit.Test
import org.codehaus.groovy.grails.plugins.web.taglib.RenderTagLib
import uk.co.desirableobjects.ajaxuploader.exception.MissingRequiredAttributeException
import uk.co.desirableobjects.ajaxuploader.exception.UnknownAttributeException
import uk.co.desirableobjects.ajaxuploader.exception.InvalidAttributeValueException

class AjaxUploaderTagLibTests extends TagLibUnitTestCase {

    static final String uploaderUid = 'testAjaxUploader'
    static final Map EXAMPLE_PARAMETERS = [myKey: 'myValue', myOtherKey: 5]
    static final String DUMMY_PLUGIN_CONTEXT_PATH = 'plugins/ajax-uploader-1.0'
    static final String DUMMY_CALLBACK = "alert(filename+' yadda yadda')"

    protected void setUp() {
        super.setUp()
        // TODO: If grails taglib testing wasn't so utterly obscure, this could be better.
        AjaxUploaderTagLib.metaClass.createLink = { attrs -> return "/file/upload" }
        AjaxUploaderTagLib.metaClass.javascript = { attrs, body ->
            if (attrs.library) {
                return """<script type="text/javascript" src="${attrs.plugin}/js/fileuploader.js"></script>"""
            }
            return """<script type="text/javascript">${body}</script>"""
        }
        AjaxUploaderTagLib.metaClass.resource = { attrs -> return "/${attrs.dir}/${attrs.file}" }
        AjaxUploaderTagLib.metaClass.pluginContextPath = DUMMY_PLUGIN_CONTEXT_PATH

    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCssInclude() {

        tagLib.head([:], "")

        assertContains '<style type="text/css" media="screen">'
        assertContains "@import url( /${DUMMY_PLUGIN_CONTEXT_PATH}/css/uploader.css );"
        assertContains '</style>'

    }

    void testJsInclude() {

         tagLib.head([:], "")

         assertContains """<script type="text/javascript" src="ajax-uploader/js/fileuploader.js">"""

    }

    void testExcludeCss() {

        tagLib.head([css:'/myapp/mycss.css'], "")

        assertDoesNotContain '@import url( /css/uploader.css )'
        assertContains '@import url( /myapp/mycss.css )'

    }

    void testTagRequiresId() {

       shouldFail(MissingRequiredAttributeException.class) {
            tagLib.uploader([:], "")
       }

    }

    void testOutputsBasicUploaderContainer() {

        tagLib.uploader([id:uploaderUid, url:[]], "")

        assertContains """<div id="au-${uploaderUid}">"""

    }

    void testCreatesJavascriptFileUploader() {

        tagLib.uploader([id:uploaderUid, url:[controller:'file', action:'upload']], "")

        assertContains '<script type="text/javascript">'
        assertContains 'var au_testAjaxUploader = new qq.FileUploader({'
        assertContains "element: document.getElementById('au-testAjaxUploader')"
        assertContains "action: '/file/upload'"
        assertContains '</script>'

    }

    void testMissingUrlParameterUsesDefaultController() {

        tagLib.uploader([id:uploaderUid], "")

        assertContains "action: '/ajaxUpload/upload'"

    }

    void testValidAttributeValue() {

        tagLib.uploader([id:uploaderUid, url:[], debug:'false'], "")

        assertContains "debug: false"

    }

    void testInvalidAttributeValue() {

        shouldFail(InvalidAttributeValueException.class) {
            tagLib.uploader([id:uploaderUid, url:[], debug:'unfail'], "")
        }

    }

    void testUnknownAttribute() {

        shouldFail(UnknownAttributeException.class) {
            tagLib.uploader([id:uploaderUid, url:[], notknown:'anyvalue'], "")
        }

    }

    void testInvalidParamsBlock() {

        shouldFail(InvalidAttributeValueException.class) {
            tagLib.uploader([id:uploaderUid, url:[], params:"invalid"], "")
        }

    }

    void testParamsBlock() {

        tagLib.uploader([id:uploaderUid, url:[], params:EXAMPLE_PARAMETERS], "")

        assertContains '''myKey: 'myValue', myOtherKey: 5'''

    }

    void testSeparatelyHandledAttributes() {

        tagLib.uploader([id:uploaderUid, url:[], params:EXAMPLE_PARAMETERS], "")

        assertDoesNotContain "[myKey: myValue, myOtherKey: 5]"
        assertDoesNotContain "id:testAjaxUploader"
        assertDoesNotContain "url:[]"

    }

    void testOnCompleteCallback() {

        String onCompleteFunction = "alert(filename+' is complete')"

        tagLib.uploader([id:uploaderUid],
            tagLib.onComplete([:], { return onCompleteFunction })
        )

        assertContains "onComplete: function(id, fileName, responseJSON) { ${onCompleteFunction} }"

    }

    void testOnSubmitCallback() {

        String onSubmitFunction = "alert(filename+' is submitted')"

        tagLib.uploader([id:uploaderUid],
            tagLib.onSubmit([:], { return onSubmitFunction })
        )

        assertContains "onSubmit: function(id, fileName) { ${onSubmitFunction} }"

    }

    void testOnProgressCallback() {

        tagLib.uploader([id:uploaderUid],
            tagLib.onProgress([:], { return DUMMY_CALLBACK })
        )

        assertContains "onProgress: function(id, fileName, loaded, total) { ${DUMMY_CALLBACK} }"

    }

    void testOnCancelCallback() {

        tagLib.uploader([id:uploaderUid],
            tagLib.onCancel([:], { return DUMMY_CALLBACK })
        )

        assertContains "onCancel: function(id, fileName) { ${DUMMY_CALLBACK} }"

    }

    void testShowMessage() {

        tagLib.uploader([id:uploaderUid],
            tagLib.showMessage([:], { return DUMMY_CALLBACK })
        )

        assertContains "showMessage: function(message) { ${DUMMY_CALLBACK} }"

    }

    private assertContains(String expected) {

        assertTrue tagLib.out.toString().contains(expected)

    }

    private assertDoesNotContain(String unexpected) {
        assertFalse tagLib.out.toString().contains(unexpected)
    }
}
