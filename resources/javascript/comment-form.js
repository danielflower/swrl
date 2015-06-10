import http from './http.js';
import editor from './editor.js';

class CommentForm {
    constructor($, form) {
        this.editor = new editor.RichTextEditor($(form).find('div.rte'));
        this.$form = $(form);
        this.$addButton = this.$form.find('input[type=submit]');

        $(form).submit(() => {
            this.setLoading();
            var swirlId = parseInt($(form).find('.swirl-id-field').val(), 10);
            var commentHtml = this.editor.getHtmlContent();
            if (commentHtml) {
                http.post('/swirls/' + swirlId + '/comment', {comment: commentHtml})
                    .then(() => {
                        this.resetForm();
                    });
            }
            return false;
        });

    }

    setLoading() {
        this.$addButton.addClass('button-loading');
        this.$addButton.prop("disabled",true);
    }

    resetForm() {
        this.$addButton.prop("disabled",false);
        this.$addButton.removeClass('button-loading');
        this.editor.clear();
    }

}

var init = function ($) {
    $('form.comment').each((i, f) => new CommentForm($, f));
};

module.exports = {init: init};