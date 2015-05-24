$(document).ready(function () {
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

    var addUser = function (textbox) {
        var nameOrEmail = textbox.value;
        var label = $(document.createElement('label'));
        $(textbox).before(label);
        var cb = $(document.createElement('input'))
            .attr('type', 'checkbox')
            .attr('name', 'who')
            .attr('value', nameOrEmail)
            .attr('checked', 'checked');
        label.append(cb);
        label.append(document.createTextNode(nameOrEmail));
        textbox.value = '';
    };

    $('.user-select-box input').keypress(function (event) {
        if (event.which == 13) {
            addUser(this);
            return false;
        }
    });


    var chrome = window.chrome;
    if (chrome && chrome.app) {
        $('.chrome-only').css('display', 'block');

        if (chrome.app.isInstalled) {
            // Note: this always returns false even when installed
            $('.chrome-extension-installed').css('display', 'block');
        } else {
            $('.install-chrome-extension-box').css('display', 'block');
            $('.add-to-chrome-button').click(function () {
                console.log('running');
                chrome.webstore.install(undefined,
                    function (suc) {
                        $('.chrome-extension-installed').css('display', 'block');
                        $('.install-chrome-extension-box').css('display', 'none');
                        console.log('Installation succeeded', suc);
                    },
                    function (err) {
                        console.log('Installation failed', err);
                    }
                );
            });
        }
    }

});
