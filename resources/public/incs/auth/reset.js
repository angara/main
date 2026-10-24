
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


$(function() {
  var URL_CODE = "/auth/reset/get-code";
  var URL_PASS = "/auth/reset/set-pass";

  var CODE_MIN_LENGTH = 4;
  var PASS_MIN_LENGTH = 4;

  var ON_CHANGE_EVENTS = "blur change keyup keypress cut paste";

  var qp = parse_qs();

  var $email = $("#email");
  var $code  = $("#code");
  var $pass  = $("#pass");

  var $msg_code = $("#msg_code");
  var $msg_pass = $("#msg_pass");

  var $btn_code = $("#get_code");
  var $btn_pass = $("#set_pass");


  function msg_ok( $fld, text ) {
    $fld.addClass("msg_ok").removeClass("msg_err").text(text);
  }

  function msg_err( $fld, text ) {
    $fld.addClass("msg_err").removeClass("msg_ok").text(text);
  }

  function msg_wait( $fld, text ) {
    $fld.removeClass("msg_err").removeClass("msg_ok")
      .html("")
      .append("<i class='fa fa-spinner fa-spin fa-fw'></i>")
      .append( $("<span/>").text(text) );
  }

  function validate_email()
  {
    var eml = $email.val().trim();
    if( eml.match(/^[0-9a-z\.\-\_]+@([0-9a-z\-]+\.)+([a-z]){2,}$/i) ) {
      $email.parent().removeClass("has-error");
      return eml;
    }
    else {
      $email.parent().addClass("has-error");
      return false;
    }
  }

  function validate_code()
  {
    var code = $code.val().trim();

    if(code.length < CODE_MIN_LENGTH) {
      $code.parent().addClass("has-error");
      return false;
    }
    else {
      $code.parent().removeClass("has-error");
      return code;
    }
  }

  function validate_pass()
  {
    var pass = $pass.val();

    if(pass.length < PASS_MIN_LENGTH) {
      $pass.parent().addClass("has-error");
      return false;
    }
    else {
      $pass.parent().removeClass("has-error");
      return pass;
    }
  }

  $email.on(ON_CHANGE_EVENTS, function(){ validate_email();});
  $code.on( ON_CHANGE_EVENTS, function(){ validate_code(); });
  $pass.on( ON_CHANGE_EVENTS, function(){ validate_pass(); });

  $email.val(qp.email || "");
  $code.val(qp.code || "");

  if(qp.code) { $pass.focus(); }
  else { $email.focus(); }


  $("#get_code").click( function(evt) {
    evt.preventDefault();
    var email = validate_email();
    if( !email ) {
      $email.focus();
    }
    else {
      $btn_code.prop("disabled", true);
      msg_wait( $msg_code, " Запрос ..." );

      lib.post(URL_CODE, {email:email},
        function(data) {
            msg_ok( $msg_code, data.msg );
        },
        function (err) {
          msg_err( $msg_code, err.msg );
          if(err.focus) { $(err.focus).focus(); }
        }
      ).complete(
        function() {
          setTimeout(
            function(){ $btn_code.prop("disabled", false); },
            5000
          );
        }
      );
    }
  });

  $("#set_pass").click( function(evt) {
    evt.preventDefault();
    var email = validate_email();
    var code  = validate_code();
    var pass  = validate_pass();

    if(email && code && pass)
    {
      $btn_pass.prop("disabled", true);
      msg_wait( $msg_pass, " Запрос ..." );

      lib.post(
        URL_PASS,
        {email:email, code:code, pass:pass},
        function (data) {
          msg_ok( $msg_pass, data.msg );
          if(data.redir) {
            setTimeout(
              function(){ location.href=data.redir; },
              3000
            );
          }
        },
        function(err) {
          msg_err( $msg_pass, err.msg );
          if(err.focus) { $(err.focus).focus(); }
        }
      ).complete(
        function() {
          setTimeout(
            function(){ $btn_pass.prop("disabled", false); },
            5000
          );
        }
      );
    }
    else {
      if(!email) { $email.focus(); return; }
      if(!code)  { $code.focus(); return; }
      if(!pass)  { $pass.focus(); return; }
    }
  });
});

//.
