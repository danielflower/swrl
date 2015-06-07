var setup = function () {

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
};

module.exports = setup;