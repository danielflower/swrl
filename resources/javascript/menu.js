var setup = function ($) {
    var header = $('body');

    $('.menu-button').click(() => {
        header.toggleClass('menu-open');
        //bigMenu.slideToggle();
        return false;
    });

};

module.exports = {init: setup};