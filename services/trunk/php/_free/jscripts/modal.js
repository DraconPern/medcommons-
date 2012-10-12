var cssTest;

$(document).ready(function() {
    //check if CSS is enabled or not if not, don't run timeline javascripts
    cssTest = $('#mask').css('z-index');

    if (cssTest == '9000') {
        //select all the a tag with name equal to modal
        $('a[name=modal]').click(function(e) {
            //Cancel the link behavior
            e.preventDefault();

            //Get the A tag
            var id = $(this).attr('href');

            //Get the screen height and width
            var maskHeight = $(document).height();
            var maskWidth = $(document).width();

            //Set heigth and width to mask to fill up the whole screen
            $('#mask').css({ 'width': maskWidth, 'height': maskHeight });

            //transition effect		
            $('#mask').fadeIn(300);
            $('#mask').fadeTo(300, 0.8);

            //Get the window height and width
            var winH = $(window).height();
            var winW = $(window).width();

            //Set the popup window to center
            $(id).css('top', winH / 2 - $(id).height() / 2);
            $(id).css('left', winW / 2 - $(id).width() / 2);

            //transition effect
            $(id).fadeIn(500);
            $('.window .modal-close').focus();
        });


        //if close button is clicked
        $('.window .modal-close').click(function (e) {
            //Cancel the link behavior
            e.preventDefault();
            $('#mask').hide();
            $('.window').hide();
            //alert($(this).attr('id').split('_')[1]);
            $('#modallink' + $(this).attr('id').split('_')[1]).focus();
        });

        //if mask is clicked
        $('#mask').click(function() {
            $(this).hide();
            $('.window').hide();

            //window.location.href = window.location.href + '#mainbody';
        });
    }
});