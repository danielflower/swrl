var setup = function ($) {
    var header = $('.site-header');
    var bigMenu = $('.big-menu');

    $('.menu-button').click(() => {
        header.toggleClass('menu-open');
        bigMenu.slideToggle();
        return false;
    });

};

module.exports = {init: setup};