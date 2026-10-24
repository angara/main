
$(function(){
  var URL_UPDATE = "/me/update"
  var ON_CHANGE_EVENTS = "blur change keyup keypress cut paste";

  // validators

  function validate_fname(fld, val)
  {
    var val = val.trim();
    if( val.match(/^([a-zа-яЁё]){1,40}$/i) ) { return val; }
    else { return false; }
  }

  function validate_lname(fld, val)
  {
    var val = val.trim();
    if( val.match(/^([a-zа-яЁё]){1,40}$/i) ) { return val; }
    else { return false; }
  }

  function validate_town(fld, val)
  {
    var val = val.trim();
    if( val.match(/^([a-zа-яЁё0-9\.\,\-\ ]){2,60}$/i) ) { return val; }
    else { return false; }
  }

  function validate_about(fld, val)
  {
    return val.trim();
  }

  //

  function _trap(fld, val)
  {
    console.error("unexpected fld:", fld);
    return false;
  }

  // *** **** *** //

  function get_validator(fld)
  {
    return {
      first_name: validate_fname,
      last_name:  validate_lname,
      town:  validate_town,
      about: validate_about
    }[fld] || _trap;
  }

  function validate_input($inp)
  {
    var v = $inp.data("validator")($inp.data("fld"), $inp.val());
    if(v) {
      $inp.closest(".form-group").removeClass("has-error");
      return v;
    }
    else {
      $inp.closest(".form-group").addClass("has-error");
      return false;
    }
  }

  function btn_spinner($btn, spin) {
    if(spin) {
      $btn.prop("disabled", true)
        .find("i").removeClass("fa-check").addClass("fa-spinner fa-spin");
    }
    else {
      $btn.prop("disabled", false)
        .find("i").removeClass("fa-spinner fa-spin").addClass("fa-check");
    }
  }

  function on_save($btn) {
    var fld = $btn.data("fld");
    var v = validate_input( $("#"+fld) );
    if(!v) { return; }

    btn_spinner($btn, true);
    lib.post(
      URL_UPDATE,
      {fld:fld, val:v},
      function (resp) {
        // title:
        $.notify(
          {
            message:"Данные сохранены.",
            icon:   "fa fa-check"
          },
          {type:"success"}
        );
        btn_spinner($btn, false);
      },
      function (err) {
        $.notify(
          {
            message: " "+(err.msg || "Ошибка!"),
            icon: "fa fa-exclamation-circle"
          },
          {type:"danger"}
        );
        btn_spinner($btn, false);
      }
    );
  }

  $(".b-me_info").find("[data-fld]").each( function(i, el) {
    var $el = $(el);
    var fld = $el.data("fld");

    $el.find("input")
      .data({validator:get_validator(fld), fld:fld})
      .on(ON_CHANGE_EVENTS, function(evt) {
        validate_input( $(evt.currentTarget) );
      });

    $el.find("button")
      .data({fld:fld})
      .on("click", function(evt) {
        evt.preventDefault();
        on_save( $(evt.currentTarget) );
      });
  });

  $('#btn_logout').click(function(evt){
    evt.preventDefault();
    lib.post('/auth/logout', {}, function(resp){
      if(resp.redir){ window.location.href = resp.redir; }
    });
  });

  $.notifyDefaults({
    delay: 4000,
    offset: { x:10, y:12 },
  	placement: {
  		from: "top"
  	},
  	animate:{
  		enter: "animated fadeInUp",
  		exit: "animated fadeOutDown"
  	}
  });
});

//.
