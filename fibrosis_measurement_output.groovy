import qupath.lib.gui.tools.MeasurementExporter
import qupath.lib.objects.PathAnnotationObject

// Get the list of all images in the current project
def project = getProject()
def imagesToExport = project.getImageList()

// Separator for CSV
def separator = ","

// Include all columns by leaving the array empty
def columnsToInclude = new String[0]

// Export type: annotation objects
def exportType = PathAnnotationObject.class


// Define the classes you want to measure
def classesToMeasure = ["Collagen", "cardiomyocyte", "adipose", "background"]


// Output path with .csv extension
def outputPath = "/output/folder/annotation_measurements.csv"
def outputFile = new File(outputPath)

// Create and run the exporter
def exporter  = new MeasurementExporter()
                  .imageList(imagesToExport)
                  .separator(separator)
                  .includeOnlyColumns(columnsToInclude)
                  .exportType(exportType)
                  .filter(obj -> obj.getPathClass() == getPathClass("Tissue")) // Filter for 'Tissue' annotations
                  .exportMeasurements(outputFile)

print "Done exporting all annotation measurements as CSV!"
