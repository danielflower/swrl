import http from './http.js';
import editor from './editor.js';

class CommentForm {
    constructor($, form) {
        this.editor = new editor.RichTextEditor($(form).find('div.rte'));
        this.$form = $(form);
        this.$addButton = this.$form.find('input[type=submit]');
        this.$maxCommentIdField = this.$form.find('.max-comment-id-field');
        this.swirlId = parseInt($(form).find('.swirl-id-field').val(), 10);
        this.refreshToken = null;

        $(form).submit(() => {
            this.setLoading();

            var commentHtml = this.editor.getHtmlContent();
            if (commentHtml) {
                http.post('/swirls/' + this.swirlId + '/comment', {comment: commentHtml})
                    .then(this.addMissingComments.bind(this))
                    .then(() => {
                        this.resetForm();
                    });
            }
            return false;
        });

        this.addMissingComments();

    }

    addMissingComments() {
        if (this.refreshToken) {
            clearTimeout(this.refreshToken);
        }
        var me = this;
        return http.getJson('/swirls/' + me.swirlId + '/comments?comment-id-start=' + me.$maxCommentIdField.val())
            .then(comments => {
                if (comments.count > 0) {
                    $('.comments').append(comments.html);
                    me.$maxCommentIdField.val(comments.maxId);
                }
                me.refreshToken = setTimeout(me.addMissingComments.bind(me), 30000);

            });
    }

    setLoading() {
        this.$addButton.addClass('button-loading');
        this.$addButton.prop("disabled", true);
    }

    resetForm() {
        this.$addButton.prop("disabled", false);
        this.$addButton.removeClass('button-loading');
        this.editor.clear();
    }

}

var init = function ($) {
    $('form.comment').each((i, f) => new CommentForm($, f));
};

module.exports = {init: init};