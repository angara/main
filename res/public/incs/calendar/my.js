
$(function(){

  var minDate = new Date();
  var maxDate = new Date(minDate.getTime() + 365*24*3600*1000);

  $(".b-calendar .b-my .b-crec input.date").each( function(i, el){
    $(el).datepicker({
      minDate:   minDate,
      maxDate:   maxDate,
      autoClose: true
    });
  });
});
