//
//  forum/api routines
//

//
// authorized users only
//
if(window.uid) {

  //
  // vote msg
  //

  $(function(){
    var ADD_TOPIC = "/calendar/add-topic";

    var tid = $("#topic_edit_btn_id").data("tid");
    if(!tid) { return false; }

    function topic_add(evt)
    {
      evt.preventDefault();
      $.post(ADD_TOPIC, {tid:tid}, function(resp){
        if(resp.redir) {
          window.location.href=resp.redir;
        }
        else {
          alert(resp.msg || "Ошибка при добавлении в календарь!");
        }
      });
    };

    $.getJSON(ADD_TOPIC,{tid:tid}, function(resp)
    {
      if(resp.allowed) {
        $("#topic_title").append(
          $("<div class='topic_edit_btn'/>")
            .append("[")
            .append(
              $("<a href='#'>в календарь</a>").click(topic_add)
            ).append("]")
        );
      }
    });
  });
}
//

//.
