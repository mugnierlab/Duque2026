setImageType('FLUORESCENCE')
setChannelNames(
     'Hoescht',
     'WGA',
     'CD20',
     'CD79a'
)

createAnnotationsFromPixelClassifier("WGA_tissue", 300000.0, 5.0, "SPLIT", "INCLUDE_IGNORED")

def tissueAnnotations = getAnnotationObjects().findAll { it.getPathClass() == getPathClass("Tissue") }
selectObjects(tissueAnnotations)

qupath.ext.instanseg.core.InstanSeg.builder()
    .modelPath("/Users/carolinaduque/QuPath/v0.7/instanseg/downloaded/single_channel_nuclei-0.1.0")
    .device("mps")
    .inputChannels([ColorTransforms.createChannelExtractor("Hoescht")])
    .outputChannels()
    .tileDims(512)
    .interTilePadding(64)
    .nThreads(4)
    .makeMeasurements(true)
    .randomColors(true)
    .outputType("Default")
    .build()
    .detectObjects()

println "expanding nuclei"
// Convert nuclei to cells, with a fixed expansion (here 10 pixels) @author Pete Bankhead: QuPath-Convert nuclei to cells.groovy
def detections = getDetectionObjects().findAll {d -> !d.isCell()}
def cells = CellTools.detectionsToCells(detections, 2, -1)
removeObjects(detections, true)
addObjects(cells)

println "running classification"
selectObjects(cells)
runObjectClassifier("CD20_classifier","CD79a_classifier")
