<%-- NOTE: due to some hackery, this is also duplicated in WADO.js in the displayCurrentImage function. Needs to be reworked. --%>
<img id="wadoImage" name="image" width="0" height="0" src="blank.png" onload="imageLoadCompleted();" onabort="imageLoadAborted();" onerror="imageLoadError();" onmousedown="beginDrag(this,event);" />

