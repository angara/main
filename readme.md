
# Angara.Net: main page app

'''
https://mothereff.in/js-escapes
'''


'''
A code point C greater than 0xFFFF corresponds to a surrogate pair <H, L> as per the following formula:

H = Math.floor((C - 0x10000) / 0x400) + 0xD800
L = (C - 0x10000) % 0x400 + 0xDC00

The reverse mapping, i.e. from a surrogate pair <H, L> to a Unicode code point C, is given by:

C = (H - 0xD800) * 0x400 + L - 0xDC00 + 0x10000

'''



```

<div class="b-forum-topic" style="margin: 0px 1em;">
<div class="topic_title" id="topic_title">
<div class="topic_edit_btn">[<a href="#" id="topic_edit_btn_id" data-tid="114326"
    >изменить</a>]</div>

<script type="text/javascript">
$(function(){

    var $e_btn = $("#topic_edit_btn_id");
    $e_btn.click(function(evt){

        function cleanup(){
            $(".topic_title_edit").remove();
            $e_btn.removeClass("disabled");
        }

        if( $e_btn.hasClass("disabled") ) {
            cleanup();
            return false;
        }
        $e_btn.addClass("disabled");

        var tid = $e_btn.data("tid");
        var $ttext = $("#topic_title_text");
        $("#topic_title").after(
            $('<form id="topic_title_edit_trm" class="topic_title_edit b-form"/>')
                .append(
                    $('<input type="text" class="inp" id="topic_titile_edit_fld"/>').val($ttext.text())
                ).append(
                    '<button style="margin-left: 4px;">Сохранить</button>'
                )
                .submit(function(){
                    var txt = $("#topic_titile_edit_fld").val();

                    $("#topic_title_edit_trm > input").attr("disabled", 1);
                    $("#topic_title_edit_trm > button").attr("disabled", 1);

                    $.ajax({
                        type: "POST",
                        url: "/forum/api/topic/title",
                        data: {tid:tid, title:$("#topic_titile_edit_fld").val()},
                        dataType: 'json'
                    })
                    .error(function(){flash_msg("Ошибка при обращении к серверу!");})
                    .success(function(data){
                        if(data.ok) {
                            $ttext.text( data.title );
                            cleanup();
                            return;
                        }
                        // if( data.err )
                        flash_msg(data.msg);
                    });



                    return false;
                })
        );
        return false;
    });
});
</script>
<div class="topic_edit_btn"><small>(<em>1</em>)&nbsp;</small> [<a href="/admin/forum-topic.jsp?tid=114326" target="_blank">adm</a>]</div><span id="topic_title_text">Рафтинг-Мания. Приглашаем на открытие сплавного сезона 2017.</span></div><div class="b-forum">


```
