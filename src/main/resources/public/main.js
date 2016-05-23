$(function(){
  $('body').on('applicationError', function(elt, msg){
    $.jGrowl(msg, { header: 'ERROR' });
  });
})

Intercooler.ready(function(){
  $("#ticker-input").focus()
})