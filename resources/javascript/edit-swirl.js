var setup = function () {

    var addUser = function (textbox) {
            var nameOrEmail = textbox.value;
            var allUsersList = textbox.getAttribute('list');
            var allUsersOptions = document.querySelectorAll('#' + allUsersList + ' option');
            var userHTML = false;

            for(var i = 0; i < allUsersOptions.length; i++) {
                    var option = allUsersOptions[i];

                    if(option.innerText === nameOrEmail) {
                        userHTML = option.getAttribute('data-value');
                        break;
                    }
                }

            if(userHTML){
                $(textbox).before(userHTML);
            }else {
                var input = $(document.createElement('input'))
                                .attr('type', 'checkbox')
                                .attr('name', 'who')
                                .attr('value', nameOrEmail)
                                .attr('checked', 'checked')
                                .attr('id', nameOrEmail);
                var label = $(document.createElement('label'))
                                .attr('for', nameOrEmail)
                                .attr('class', 'no-avatar');
                $(textbox).before(input);
                $(textbox).before(label);
                label.append(document.createTextNode(nameOrEmail));
            }
            var br = $(document.createElement('div'))
                         .attr('class', 'small-padding');
            $(textbox).before(br);
            textbox.value = '';
        };

    $('.user-select-box input').keydown(function (event) {
       if (event.which == 13 || event.which == 9) {
           event.preventDefault();
           addUser(this);
           return false;
       }
   });
};

module.exports = setup;