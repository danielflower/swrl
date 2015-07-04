class RichTextEditor {
    constructor($rteDiv) {
        this.$textarea = $rteDiv.find('textarea').first();
        this.$editorDiv = $rteDiv.find('.editor').first();

        $rteDiv.find('.spoiler-alert-button').click(() => {
            this.addHtmlAtCursor('<div class="spoiler-alert">' +
                '<div class="spoiler-alert--bar" title="Click to expand" contenteditable="false">' +
                '<button class="spoiler-alert--close-button" title="Delete spoiler alert">x</button>' +
                '<a href="#">Spoiler alert</a>' +
                '</div>' +
                '<div class="spoiler-alert--content" data-ph="Write your spoilers here - they will not be shown unless clicked on"></div>' +
                '</div>' +
                '<p data-ph="..."></p>');
            // HACK: this is repeated below and is just adding more and more click handlers each time
            $('.spoiler-alert--close-button').click((b) => {
                $(b.target).closest('.spoiler-alert').remove();
                return false;
            });

            return false;
        });


        var html = this.$textarea.val();
        this.$editorDiv.html(html);


        $rteDiv.closest("form").on("submit", () => {
            this.$textarea.val(this.getHtmlContent());
            return true;
        });
    }

    addHtmlAtCursor(html) {
        this.$editorDiv.append(html);
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
        new RichTextEditor($(holder));
    });

};

var initWidgets = function ($) {
    $('.spoiler-alert--bar a').click((b) => {
        $(b.target).closest('.spoiler-alert').find('.spoiler-alert--content').toggle();
        return false;
    });

    $('.spoiler-alert--close-button').click((b) => {
        $(b.target).closest('.spoiler-alert').remove();
        return false;
    });
};

module.exports = {init: setup, RichTextEditor: RichTextEditor, initWidgets: initWidgets};