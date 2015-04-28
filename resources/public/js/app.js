
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
});
