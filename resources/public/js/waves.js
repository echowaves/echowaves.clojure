function update_waves_picker() {
    $("#waves-picker").empty();
    $.getJSON( "/all-my-waves.json", function( data ) {
        var items = [];

        $.each( data, function( index, item ) {
            items.push( "<option id='" + item.id + "' value='" + item.name + "'>" + 
                         item.name + "</option>");
        });

        $("#waves-picker").append(items);
    });
}


update_waves_picker();
