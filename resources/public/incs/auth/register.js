
$(function(){
  var URL_REGISTER = "/auth/register";
  var URL_RESET = "/auth/reset";
  var SPINNER = " <i class='fa fa-spinner fa-spin fa-fw'></i>";
  var ON_CHANGE_EVENTS = "blur change keyup keypress cut paste";

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

  var $btn_register = $("#btn_register");
  var $msg = $("#msg");

  function msg_err( text ) {
    $msg.html( text || "Ошибка при обращении к серверу!" );
    setTimeout(
      function() { $msg.html("&nbsp;"); },
      5000
    );
  }

  var $email      = $("#email");
  var $username   = $("#username");
  var $first_name = $("#first_name");
  var $last_name  = $("#last_name");
  var $town       = $("#town");

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

  function validate_username()
  {
    var un = $username.val().trim();
    if( un.match(/^([a-z][0-9a-z\_\.\-]*){4,32}$/i) ) {
      $username.parent().removeClass("has-error");
      return un;
    }
    else {
      $username.parent().addClass("has-error");
      return false;
    }
  }

  function validate_fname()
  {
    var fname = $first_name.val().trim();
    if( fname.match(/^([a-zа-яЁё]){1,40}$/i) ) {
      $first_name.parent().removeClass("has-error");
      return fname;
    }
    else {
      $first_name.parent().addClass("has-error");
      return false;
    }
  }
  function validate_lname()
  {
    var lname = $last_name.val().trim();
    if( lname.match(/^([a-zа-яЁё]){1,40}$/i) ) {
      $last_name.parent().removeClass("has-error");
      return lname;
    }
    else {
      $last_name.parent().addClass("has-error");
      return false;
    }
  }

  function validate_town()
  {
    var town = $town.val().trim();
    if( town.match(/^([a-zа-яЁё0-9\.\,\-\ ]){1,60}$/i) ) {
      $town.parent().removeClass("has-error");
      return town;
    }
    else {
      $town.parent().addClass("has-error");
      return false;
    }
  }

  $email.on(ON_CHANGE_EVENTS, function(){ validate_email();});
  $username.on( ON_CHANGE_EVENTS, function(){ validate_username(); });
  $first_name.on( ON_CHANGE_EVENTS, function(){ validate_fname(); });
  $last_name.on( ON_CHANGE_EVENTS, function(){ validate_lname(); });
  $town.on( ON_CHANGE_EVENTS, function(){ validate_town(); });

  $btn_register.click(function(evt)
  {
    evt.preventDefault();

    var email = validate_email();
    if(!email) { $email.focus(); return false; }

    var username = validate_username();
    if(!username) { $username.focus(); return false; }

    var first_name = validate_fname();
    if(!first_name) { $first_name.focus(); return false; }

    var last_name = validate_lname();
    if(!last_name) { $last_name.focus(); return false; }

    var town = validate_town();
    if(!town) { $town.focus(); return false; }

    btn_spinner( $btn_register, true );

    lib.post(
      URL_REGISTER,
      { email:email, username:username, town:town,
        first_name:first_name, last_name:last_name
      },
      function(data) {
        var u = URL_RESET+"?email="+data.email;
        $msg.addClass("msg_ok").removeClass("msg_err").html(
          "Для завершения регистрации введите полученный на E-mail код"+
          " и установите пароль на <a href='"+ u +"'>этой странице</a>."
        );
        setTimeout(
          function(){ window.location.href = u; },
          5000
        )
      },
      function(err) {
        msg_err(err.msg);
        btn_spinner( $btn_register, false );
        if(err.fld) {
          $(err.fld).focus();
        }
      }
    );
  });

  $("#email").focus();
});

//.
