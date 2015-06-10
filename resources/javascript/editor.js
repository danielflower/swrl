class RichTextEditor {
    constructor($rteDiv) {
        this.$textarea = $rteDiv.find('textarea');
        this.$editorDiv = $rteDiv.find('.editor');
    }

    getHtmlContent() {
        return this.$editorDiv.html().trim();
    }

    clear() {
        this.$textarea.val('');
        this.$editorDiv.html('');
    }
}


var setup = function ($) {
    $(".rte").each(function (i, holder) {
        var textarea = $(holder).find("textarea").first();
        var editor = $(holder).find(".editor").first();

        var html = textarea.val();
        editor.html(html);

        $(holder).closest("form").on("submit", function () {
            textarea.val(editor.html());
            return true;
        });
    });
};


module.exports = { init: setup, RichTextEditor: RichTextEditor };