////////////////////
// ACTIVATING TABS BASED ON URL
////////////////////
$(function () {
    var activeTab = $('[href=' + location.hash + ']');
    //    activeTab && activeTab.tab('show');
    activeTab.tab('show');
});

////////////////////
// ACTIVATING TABS BY CLICKING
////////////////////
$('#myTab a').click(function (e) {
    e.preventDefault();
    $(this).tab('show');
    var id = $(e.target).attr("href").substr(1);
    window.location.hash = id;
    $('html,body').scrollTop(0); // go to top!
    //$('html, body').animate({ scrollTop: 0 }, 'fast'); // animate and go to top
    //       $(".navbar-toggle").click() //bootstrap 3.x by Richard
});

$(document).on('click','.navbar-collapse.in',function(e) {
    if( $(e.target).is('a') && $(e.target).attr('class') != 'dropdown-toggle' ) {
        $(this).collapse('hide');
    }
});

reolive.WebReo.main(document.getElementById('contentWrap'));
