
$(function(){
  var URL_LOGIN = "/auth/login";
  var URL_RESET = "/auth/reset";
  var SPINNER = " <i class='fa fa-spinner fa-spin fa-fw'></i>";

  function parse_qs()
  {
    var parsedParameters = {};
    var qs = location.search.substr(1);
    var uriParameters = qs.split('&');

    for (var i = 0; i < uriParameters.length; i++)
    {
      var p = uriParameters[i].split('=');
      parsedParameters[p[0]] = decodeURIComponent(p[1]);
    }

    return parsedParameters;
  }

  function btn_spinner($btn, show) {
    if(show) {
      $btn.data("btn_text", $btn.html())
      $btn.prop("disabled", true);
      $btn.append( SPINNER );
    }
    else {
      $btn.html( $btn.data("btn_text") );
      $btn.prop("disabled", false);
    }
  }

  var $btn_login = $("#login");
  var $msg = $("#msg");

  function msg_err( text ) {
    $msg.html( text || "Ошибка при обращении к серверу!" );
    setTimeout(
      function() { $msg.html("&nbsp;"); },
      5000
    );
  }

  $btn_login.click(function(evt)
  {
    evt.preventDefault();

    var email = $("#email").val().trim();
    var pass  = $("#pass").val();
    var remember = $("#remember").prop("checked");

    if(email && pass) {
      btn_spinner( $btn_login, true );

      lib.post(URL_LOGIN,
        {email:email, pass:pass, remember:remember},
        function(data) {
          var redir = (parse_qs() || {}).redir || "/";
          window.location.href =
            window.location.protocol + "//" +
            window.location.host + redir;
        },
        function (err) {
          msg_err(err.msg);
          btn_spinner( $btn_login, false );
        }
      );
    }
    else {
      msg_err("Введите E-mail и пароль!");
    }
  });

  $("#email").focus();
});

//.
