
$(function(){
  $("#name").focus();
  $("#send").click( function(){

    $("#send").prop("disabled", true);
      $.ajax({
        url:  "/icestorm/register",
        type: "POST",
        dataType: "json",
        data: {
            name:  $("#name" ).val(),
            town:  $("#town" ).val(),
            email: $("#email").val(),
            phone: $("#phone").val(),
            age:   $("#age"  ).val(),
            disc:  $("#disc" ).val()
        }
      }).done( function(resp) {
          if(resp.ok) {
            $("#form").html("").append(
                $("<div>").css({
                  "margin":"2em 10em 2em 10em",
                  "text-align":"center",
                  "font-size": "150%",
                  "font-weight": "bold",
                  "color": "#0a4"
                }).text("Заявка принята / Application accepted")
              ).append(
                $("<div>").css({
                  "margin":"2em 10em 6em 10em",
                  "text-align":"center",
                }).html(
                  "Для того, что бы подтвердить участие в гонке,"+
                  " заполните, пожалуйста, <a href='http://www.ice-storm.com/images/anketa.pdf'>анкету</a>."+
                  "<br/>"+
                  "To confirm participation in the race please fill out the "+
                  " <a href='http://www.ice-storm.com/images/anketa-eng.pdf'>agreement</a>, sign, scan and send to "+
                  " <a href='mailto:icestorm@angara.net?Subject=anketa'>icestorm@angara.net</a>."+
                  "<br/><br/>"+
                  "Скан заполненной и подписанной анкеты просим Вас отправить по адресу:"+
                  " <a href='mailto:icestorm@angara.net?Subject=anketa'>icestorm@angara.net</a>."
                )
              );
          }
          else {
            $("#send").prop("disabled", false);
            if(resp.fld) { $("#"+resp.fld).focus(); }
            alert(resp.msg || "Ошибка на сервере!");
          }
      }).fail( function() {
          $("#send").prop("disabled", false);
          alert("Ошибка при обращении к серверу!");
      });
      return false;
  });
});
