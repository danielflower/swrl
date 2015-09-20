import http from './http.js';

class RichTextEditor {
    constructor($rteDiv) {
        this.$textarea = $rteDiv.find('textarea').first();
        this.$editorDiv = $rteDiv.find('.editor').first();
        this.editorDiv = this.$editorDiv[0];


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

        var me = this;
        this.$editorDiv.bind('paste', () => {
            setTimeout(me.convertPlainTextLinksToAnchorTags.bind(me), 1);
        });


        var html = this.$textarea.val();
        this.$editorDiv.html(html);


        $rteDiv.closest("form").on("submit", () => {
            this.copyInputFromEditableDivToPostableTextArea();
            return true;
        });
        this.$editorDiv.focusout(() => {
            this.copyInputFromEditableDivToPostableTextArea();
        });
    }

    copyInputFromEditableDivToPostableTextArea() {
        this.$textarea.val(this.getHtmlContent());
    }

    visitDescendents(startNode, visitor) {
        var children = startNode.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            var continueDown = visitor(child);
            if (continueDown) {
                this.visitDescendents(child, visitor);
            }
        }
    }

    enrichLinks() {
        this.$editorDiv.find('a').each((i, e) => {
            if (e.href === e.innerText && e.className !== 'transforming-link') {
                e.className = 'transforming-link';
                $(e).append('<i class="fa fa-spin fa-spinner"></i>');
                http.getJson('/website-service/get-metadata?url=' + encodeURI(e.href))
                    .then((metadata) => {
                        console.log('Got metadata', metadata);
                        e.innerText = metadata.title || e.href;
                        e.className = '';
                        var html = metadata['embed-html'];
                        if (html) {
                            $(e).after('<div class="user-entered-embed-box">' + html + '</div>');
                        }
                    });
            }
        });
    }

    convertPlainTextLinksToAnchorTags() {
        var node = this.editorDiv;
        var links = this.getHtmlContent().match(/(http(s|):\/\/[^<>\s]+(\.[^<>\s]+)*(|:[0-9]+)[^<>\s]*)/g);
        if (!links) {
            return;
        }

        this.visitDescendents(node, (e) => {
            if (e.nodeType === 3) { // text nodes
                for (var i = 0; i < links.length; i++) {
                    var htmlEncodedLink = $('<div/>').html(links[i]).text();
                    var index = e.data.indexOf(htmlEncodedLink);
                    if (index > -1) {
                        // split the text node into 3 bits - the 'nextBit' is the part containing the URL
                        var nextBit = e.splitText(index);
                        nextBit.splitText(htmlEncodedLink.length);
                        var target = (htmlEncodedLink.indexOf('http://www.swrl.co') === 0) ? '' : ' target="_blank"';
                        $(nextBit).before('<a href="' + htmlEncodedLink + '"' + target + '>' + htmlEncodedLink + '</a>');
                        nextBit.data = '\u00A0';
                        return false; // stop processing this bit - we've changed it so processing will be weird
                    }
                }
                if (!window.todo) window.todo = e;
            } else if (e.nodeType === 1 && e.tagName === 'A') {
                // This is an anchor element already - don't convert the HTML twice dawg
                return false;
            }
            return true;
        });


        this.enrichLinks();
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