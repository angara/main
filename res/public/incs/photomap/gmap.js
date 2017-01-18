//
//  Angara.Net: photomap
//

var gmap = null;

function place_markers(markers) {
  for(var i=0; i < markers.length; i++) {
    var m = markers[i];

    var marker = new google.maps.Marker({
      map: gmap,
      position: m.coord,
      title: m.caption
    });
    marker.infowindow = new google.maps.InfoWindow({
      maxWidth: 340,
      content:
          "<div>"+
            "<div>"+m.date+" - "+(m.chat.username || "")+"<br/>"+m.caption+"</div>"+
            "<div style='position:relative;'>"+
              "<a target='_blank' href='"+m.orig+"'>"+
              "<img style='margin:2px auto; display:block; width:100%; border-radius:3px;'"+
                  " src='"+m.pict+"' /></a>"+
            "</div>"+
          "</div>"
    });
    // marker.infowindow.setMaxWidth
    marker.addListener('click', function(){
      this.infowindow.open(gmap, this);
    });

  }
}

function get_markers(cb) {
  $.ajax({
    dataType: "json",
    url: "/photomap/markers",
    // data: data,
    success: function(resp) {
      cb(resp.markers)
    },
    error: function(err) {
      console.error("get_markers:", err);
    }
  });
}

function init_gmap () {
  gmap = new google.maps.Map(document.getElementById('gmap'), {
    center: {lat: 52.3, lng: 104.3},
    zoom: 12,
    mapTypeId: google.maps.MapTypeId.HYBRID
  });
  get_markers( place_markers );
}

//.
