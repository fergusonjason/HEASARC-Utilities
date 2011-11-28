// run these commands immediately after importing the individual JSON files into the "catalog" collection

// add the empty xref array to all records. Use the _id field since an index exists by default
db.catalog.update({"_id":{$exists:true}}, {$set:{"xref":[]}}, false, true);

// need to add a space between HD and number. For some reason, hdname is already right
db.catalog.find({"name":/^HD[0-9]+/i}).forEach(function(obj) {
    obj.name = obj.hdname;
    db.catalog.save(obj);
});

// push the non-troublemaker values into the xref array
db.catalog.find().forEach(function(obj) {
    // star catalogs
    if (obj.bscname != null) {
        obj.xref.push(obj.bscname);
    }
    if (obj.cpdname != null) {
        obj.xref.push(obj.cpdname);
    }
    if (obj.dmname != null) {
        obj.xref.push(obj.dmname);
    }
    if (obj.gcname != null) {
        obj.xref.push(obj.gcname);
    }
    if (obj.gliesename != null) {
        obj.xref.push(obj.gliesename);
    }
    if (obj.hdname != null) {
        obj.xref.push(obj.hdname);
    }
    if (obj.hipname != null) {
        obj.xref.push(obj.hipname);
    }
    if (obj.ppmname != null) {
        obj.xref.push(obj.ppmname);
    }
    if (obj.saoname != null) {
        obj.xref.push(obj.saoname);
    }

    // galaxy catalogs
    if (obj.messiername != null) {
        obj.xref.push(obj.messiername);
    }
    if (obj.ngc2000name != null) {
        obj.xref.push(obj.ngc2000name);
    }
    if (obj.ugcname != null) {
        obj.xref.push(obj.ugcname);
    }
    db.catalog.save(obj);
});

db.catalog.ensureIndex({xref:1});

// at this point, run the catalog-specific scripts, then run the common-finish.js script