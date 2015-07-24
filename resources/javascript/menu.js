var setup = function ($) {
    var body = $('body');

    $('.menu-button').click(() => {
        body.toggleClass('menu-open');
        window.scrollTo(0, 0);
        return false;
    });

};

module.exports = {init: setup};