$.getJSON( "/requested-blends.json", function( data ) {
    var items = [];
    $.each( data, function( index, item ) {
        items.push( "<li class='requested-blend' id='" + item.wave_id2 + "'>" + item.name + "</li>" );
    });
    $("#requested-blends").append(items);
});


$.getJSON( "/blended-with.json", function( data ) {
    var items = [];
    $.each( data, function( index, item ) {
        items.push( "<li id='" + item.id + "'>" + item.name + "</li>" );
    });
    $("#blended-with").append(items);
});

$.getJSON( "/unconfirmed-blends.json", function( data ) {
    var items = [];
    $.each( data, function( index, item ) {
        items.push( "<li class='unconfirmed-blend' id='" + item.wave_id2 + "'>" + item.name + "</li>" );
    });
    $("#unconfirmed-blends").append(items);
});



$(function() {
    function log( message ) {
        $( "<div>" ).text( message ).prependTo( "#log" );
        $( "#log" ).scrollTop( 0 );
    }
    
    $( "#wave_name" ).autocomplete({
        source: "/autocomplete-wave-name.json",
        minLength: 2,
        select: function( event, ui ) {
            log( ui.item ?
                 "Selected: " + ui.item.value + " aka " + ui.item.id :
                 "Nothing selected, input was " + this.value );
        }
    });
});
