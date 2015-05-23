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
    if (chrome && chrome.app && !chrome.app.isInstalled) {
        $('.install-chrome-extension-box').css('display', 'block');
        $('.add-to-chrome-button').click(function () {
            console.log('running');
            chrome.webstore.install(undefined,
                function (suc) { console.log('success', suc); },
                function (err) { console.log('failure', err); }
            );
        });
    }

});
