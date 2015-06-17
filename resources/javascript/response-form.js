import http from './http.js';

class RespondForm {
    constructor($, form) {
        this.$form = $(form);
        this.response = null;
        $(form).find('button').click(this.buttonClick.bind(this));

        var customInputBox = $(form).find('.custom-response');

        $(form).submit(() => {
            var swirlId = parseInt($(form).find('.swirl-id-field').val(), 10);
            var response = this.getResponse();
            $(customInputBox).val('');
            if (response) {
                this.setSelectedButton(response, 'button-loading');
                http.post('/swirls/' + swirlId + '/respond', {responseButton: response}).then(() => {
                    this.setSelectedButton(response, 'button-primary');
                });
            }
            return false;
        });


        customInputBox.keypress((e) => {
            if (e.keyCode === 13) {
                $(form).find('.custom-response-button').click();
                return false;
            }
        });
    }

    setSelectedButton(val, selectedClass) {
        var buttonIsOnScreen = false;
        var arbitraryButton = null;
        this.$form.find('button').each((i, el) => {
            $(el).removeClass('button-primary');
            $(el).removeClass('button-loading');
            if (el.value.toLowerCase() === val.toLowerCase()) {
                buttonIsOnScreen = true;
                $(el).addClass(selectedClass);
            } else {
                if (!arbitraryButton) {
                    arbitraryButton = el;
                }
            }
        });
        if (!buttonIsOnScreen) {
            var newOne = $(arbitraryButton).clone(true);
            newOne.val(val).addClass(selectedClass);
            newOne.text(val);
            this.$form.find('.response-buttons').append(newOne);
        }
    }

    buttonClick (e) {
        if (e.target.getAttribute('data-button-type') === 'custom') {
            this.response = $(e.target.form).find('.custom-response').val();
        } else {
            this.response = e.target.value;
        }
    }

    getResponse() {
        return (this.response || '').trim();
    }
}

var init = function ($) {

    $('.respond-form').each((i, f) => new RespondForm($, f));
    //new RespondForm();


};

module.exports = {init: init};