
$(function(){

  var CALENDAR_MY_URL = "/calendar/my";

  var BASE_SEL = ".b-calendar .b-my .b-crec";

  var minDate = new Date();
  var maxDate = new Date(minDate.getTime() + 365*24*3600*1000);

  function crec_id($el){
    return $el.closest("[data-id]").data("id");
  }

  function send_update(id, data) {
    data.id = id;
    lib.post(CALENDAR_MY_URL, data,
      function(resp){
      // ok
      },
      function(err) {
        alert(err.msg || "Ошибка при обращении к серверу!");
      }
    );
  }

  //

  $(BASE_SEL+" input.date").each( function(i, el){
    var $el = $(el);
    $el.datepicker({
      minDate:   minDate,
      maxDate:   maxDate,
      autoClose: true,
      onSelect: function(fd, date)
      {
        send_update(crec_id($el), {date:fd});
      }
    });
  });

  //

  $(BASE_SEL+" input.date").on("change", function(evt) {
    var fld = $(evt.currentTarget);
    send_update(crec_id(fld), {date:fld.val()});
  });

  $(BASE_SEL+" input.status").on("change", function(evt) {
    var fld = $(evt.currentTarget);
    send_update(crec_id(fld), {status:fld.is(":checked")});
  });

  $(BASE_SEL+" input.title").on("keypress", function(evt) {
    if(evt.keyCode == 13)
    {
      var fld = $(evt.currentTarget);
      send_update(crec_id(fld), {title:fld.val()});
    }
  });

});
