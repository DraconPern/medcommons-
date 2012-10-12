	#include "CacheResizeWindowLevel.h"

	/*
	 * Basic idea of program.
	 * a) there is a source DICOM file.
	 * b) there are results - JPEG files that have been resized and the appropriate intensity mapping
	 *    has been applied (if grayscale, none if color).
	 * c) Intermediate cache files are calculated that are resized versions of the original
	 *    input DICOM files. Resizing is expensive for large images (CR, Mammography) so these
	 *    files can be used for future intensity mappings.
	 *
	 * At runtime - the basic logic is:
	 * If scaled DICOM exists, use it as input.
	 * If not - generate it, then generate JPEG output.
	 * Scaled dicom output is created for either full size objects or for image subsets. The cached
	 * dicom is exactly the size and subrectangle that is to be rendered.
	 *
	 * However - it gets more complex than this because:
	 * a) generation of thumbnails.
	 * b)
	 * TODO
	 *
	 * 5. Add thumbnail logic - build second output stream. choose the smaller of the two source images to generate.
	 *
	 * If thumbnail not requested - still generate as a subset of the jpeg if a region isn't specified.
	 * if thumbnail requested - generate scaled DICOM if original is > default_width or default_height.
	 *
	 * so - for MR/CT - scaled dicom not created for thumbnails.
	 * 7. Put in regions.
	 *ReleaseDataFlagOn
	 * Logic:
	 * If scaled exists, use it as input.
	 * If not - generate it, then generate JPEG output.
	 *
	 * Arguments:
	 * <ol>
	 * <li>Output directory</li>
	 * <li>Input directory</li>
	 * <li>Image name</li>
	 * <li>maxheight (pixels)</li>
	 * <li>maxwidth (pixels)</li>
	 * <li>window</li>
	 * <li>level</li>
	 * <li> topleft x, top left y, bottom right x, bottom right y (in pixel coordinates; assume 0,0 origin) (optional)</li>
	 * </ol>
TODO: Coordinate system wrong
	 *
	 */

	#define DEBUG 0

	int windowLevelCache(int ac, char* av[], int iterCount)
	{


		if(ac < 8)
			{
			std::cerr << "Usage: " << av[0] << " OutputDirectory InputDirectory ImageName  MaxWidth MaxHeight Window Level \n";
			return EXIT_FAILURE;
			}

		int nComponents;



		InputImageType::SizeType size;
	 // Create and configure a resampling filter





		ImageIOType::Pointer gdcmImageIO = ImageIOType::New();



		int maxWidth;
		int maxHeight;
		int window;
		int level;
		double topLeftX, topLeftY, bottomRightX, bottomRightY=-16000.0;
		maxWidth = atoi(av[MAX_WIDTH]);

		maxHeight = atoi(av[MAX_HEIGHT]);
		window = atoi(av[WINDOW]);
		level = atoi(av[LEVEL]);


		if (ac > 11){

			topLeftX = atof(av[TOP_LEFT_X]);
			topLeftY = atof(av[TOP_LEFT_Y]);
			bottomRightX = atof(av[BOTTOM_RIGHT_X]);
			bottomRightY = atof(av[BOTTOM_RIGHT_Y]);
			subregionSpecified = true;
			std::cerr << "INFO:" << " topLeftX " << av[TOP_LEFT_X] << "=>" << topLeftX <<"\n";
			std::cerr << "INFO:" << " topLeftY " << av[TOP_LEFT_Y] << "=>" << topLeftY <<"\n";
			std::cerr << "INFO:" << " bottomRightX " << av[BOTTOM_RIGHT_X] << "=>" << bottomRightX <<"\n";
			std::cerr << "INFO:" << " bottomRightY " << av[BOTTOM_RIGHT_Y] << "=>" << bottomRightY <<"\n";
		}

		size[0] = maxWidth; // number of pixels along X
		size[1] = maxWidth; // number of pixels along Y
		ostringstream scratchInputImage;
		//scratchInputImage << "ct" << iterCount <<".dcm";
		//av[INPUT_IMAGE] =
		ostringstream scaledDicom;
		if (subregionSpecified){
			scaledDicom << av[CACHE_DIRECTORY] << av[INPUT_IMAGE] << "_w" << maxWidth << "_h" << maxHeight << "_TLX" << topLeftX <<"_TLY" << topLeftY << "_BRX" << bottomRightX << "_BRY" << bottomRightY<<"_scaled.dcm";
		}
		else{
			scaledDicom << av[CACHE_DIRECTORY] << av[INPUT_IMAGE] << "_w" << maxWidth << "_h" << maxHeight << "_scaled.dcm";
		}


		string scaleDicomFilename(scaledDicom.str());
		std::cerr << "INFO:" << " Argument number " << ac <<"\n";


		ReaderType::Pointer reader = ReaderType::New();
		reader->SetImageIO( gdcmImageIO );

		std::cerr << "CACHEDFILE:" << scaleDicomFilename <<"\n";;


		ifstream scale_file ( scaleDicomFilename.c_str() );



		if ( !scale_file.is_open() ) {
			// The file could not be opened; probably doesn't exist. So - generate it.
			try
				{



				ostringstream inputDICOMFile;
				inputDICOMFile <<av[INPUT_DIRECTORY]<< av[INPUT_IMAGE];
				reader->SetFileName(inputDICOMFile.str());
				reader->Update();

				if (DEBUG) {
					std::cout<< "original image info \n";
					gdcmImageIO->Print(std::cout);
				}
				nComponents = gdcmImageIO->GetNumberOfComponents();
				if (DEBUG){
					std::cout << "nComponents (original) " << nComponents << std::endl;
				}
				ImageType::RegionType region =  reader->GetOutput()->GetLargestPossibleRegion();

				ImageType::SizeType  dicomSize  = region.GetSize();
				ImageType::SizeType subregionSize;
				RGBImageType::SizeType rgbSubregionSize;
				ImageType::RegionType subRegion;
				RGBImageType::RegionType rgbSubRegion;
				ImageType::IndexType subRegionStart;
				RGBImageType::IndexType rgbSubRegionStart;

 				//typedef itk::RegionOfInterestImageFilter< ImageType,ImageType > RegionFilterType;

				GrayscaleRegionFilterType::Pointer grayscaleRegionFilter = NULL;
				RGBRegionFilterType::Pointer rgbRegionFilter = NULL;

				int inputWidth = dicomSize[0];
				int inputHeight = dicomSize[1];
				

				double scaleWidth, scaleHeight;
				if (subregionSpecified){
				  subregionSize[0] = (long unsigned int) ((bottomRightX - topLeftX) * inputWidth);
				  subregionSize[1] = (long unsigned int) ((bottomRightY - topLeftY) * inputHeight);
				  subRegionStart[0] = (int) (topLeftX*inputWidth);
				  subRegionStart[1] = (int) (topLeftY*inputHeight);

				  rgbSubregionSize[0] = (long unsigned int) ((bottomRightX - topLeftX) * inputWidth);
				  rgbSubregionSize[1] = (long unsigned int) ((bottomRightY - topLeftY) * inputHeight);

				  rgbSubRegionStart[0] = (int) (topLeftX*inputWidth);
				    rgbSubRegionStart[1] = (int) (topLeftY*inputHeight);

				  scaleWidth = (1.0 * subregionSize[0])/(1.0 * maxWidth);
				  scaleHeight = (1.0 * subregionSize[1])/(1.0 * maxHeight);
	  			  std::cerr << "INFO:" << " subregionSize " << subregionSize[0] << "," << subregionSize[1]<<"\n";
	  			  std::cerr << "INFO:" << " subregionStart " << subRegionStart[0] << "," << subRegionStart[1]<<"\n";

				  inputWidth = subregionSize[0];
				  inputHeight = subregionSize[1];
				}
				else{
				  scaleWidth = (1.0 * inputWidth)/(1.0 * maxWidth);
				  scaleHeight = (1.0 * inputHeight)/(1.0 * maxHeight);
				}







				double scale = scaleWidth;
				if (scaleHeight > scale){
					scale = scaleHeight;
				}

				std:cerr << "scale = " << scale << " dicomsize = " << dicomSize[0] <<"\n";




				// Create an affine transformation
				AffineTransformType::Pointer affineTransform = AffineTransformType::New();
				affineTransform->Scale(scale);



				int status;
				try
					{
					// Rewrite the scaled image in DICOM format
					if (nComponents == 1){
						if (subregionSpecified){
							grayscaleRegionFilter = GrayscaleRegionFilterType::New();
							subRegion.SetSize(  subregionSize  );
							subRegion.SetIndex( subRegionStart );
							grayscaleRegionFilter->SetRegionOfInterest( subRegion );
							// TODO: Need to hook this up with the output - put this filter in the middle.
							// Reember to generate cache file name.
						}
						status= generateGrayscaleCachefile(reader, (char *) scaleDicomFilename.c_str(),  nComponents,
						 	scale,  affineTransform,  size, grayscaleRegionFilter);
						}
					else{
						if (subregionSpecified){
							rgbRegionFilter = RGBRegionFilterType::New();
							rgbSubRegion.SetSize(  rgbSubregionSize  );
							rgbSubRegion.SetIndex( rgbSubRegionStart );
							rgbRegionFilter->SetRegionOfInterest( rgbSubRegion );
							//std:cout << "dicomSize = " << dicomSize[0] << ", " << dicomSize[1] << "\n";
							// TODO: Need to hook this up with the output - put this filter in the middle.
							// Reember to generate cache file name.
							std::cerr << "About to invoke generate cache file" << std::endl;
						}
						string inputFilename = inputDICOMFile.str();
						status = generateRGBCacheFile( inputFilename, (char *) scaleDicomFilename.c_str(), nComponents,
							scale,  affineTransform,  size, rgbRegionFilter);
					}
					if (status != EXIT_SUCCESS)
						return(EXIT_FAILURE);
					}
				catch (itk::ExceptionObject & e)
					{
					std::cerr << "exception in file writer " << std::endl;
					std::cerr << e.GetDescription() << std::endl;
					std::cerr << e.GetLocation() << std::endl;
					return EXIT_FAILURE;
					}
					}
				catch (itk::ExceptionObject & e)
					{
					std::cerr << "exception in file reader " << std::endl;
					std::cerr << e.GetDescription() << std::endl;
					std::cerr << e.GetLocation() << std::endl;
					return EXIT_FAILURE;
					}

		} // End of generating cached file
		else{
			std::cerr << "File already exists " << scaleDicomFilename <<"\n";;
			reader->SetFileName(scaleDicomFilename.c_str());
			reader->Update();

			//std::cout<< "scaled image info \n";
			//gdcmImageIO->Print(std::cout);

			nComponents = gdcmImageIO->GetNumberOfComponents();
		}

		if (DEBUG){
			std::cout << "nComponents(cached) " << nComponents << std::endl;
		}
		// Rescale intensities and rewrite the image in another format
		// Format determined by file extension (in this case - ".jpg")
		//
		int jpegSuccess;
		if (nComponents ==1)
			jpegSuccess = writeGreyscalePixels(window, level,maxWidth, maxHeight,  (char *) scaleDicomFilename.c_str());
		else
			jpegSuccess = writeRGBPixels(window, level,maxWidth, maxHeight,  (char *) scaleDicomFilename.c_str());

		return jpegSuccess;

	}
	int writeGreyscalePixels(int window, int level, int maxWidth,int maxHeight, char* scaleDicomFilename){
		int nComponents;


		typedef  itk::IntensityWindowingImageFilter< InputImageType,  WriteImageType >   IntensityFilterType;

		typedef itk::RescaleIntensityImageFilter<InputImageType, WriteImageType > RescaleFilterType;
		ReaderType::Pointer readCachedDICOM = ReaderType::New();


		ImageIOType::Pointer gdcmImageIO = ImageIOType::New();


		readCachedDICOM->SetImageIO(gdcmImageIO );
		readCachedDICOM->SetFileName(scaleDicomFilename);
		nComponents = gdcmImageIO->GetNumberOfComponents();

		//RescaleFilterType::Pointer rescaler = RescaleFilterType::New();
		IntensityFilterType::Pointer intensity = IntensityFilterType::New();

		//rescaler->SetOutputMinimum(   0 );
		//rescaler->SetOutputMaximum( 255 );
		/*
		 * 1 ABDOMEN 350, 40 2
		 *   BONE 100, 170 3 CEREBRUM 120, 40 4 LIVER 100, 70 5 LUNG -300, 2000 6 HEAD 80, 40 7 PELVIS 400, 40 8 POSTERIOR FOSSA 250, 80 9 SUBDURAL 150, 40 0 CALCIUM 1, 130
		 */
		 double windowMin = (double) (level - (window/2.0));
		 double windowMax = (double) (level + (window/2.0));
		intensity->SetOutputMinimum(   0 );
		intensity->SetOutputMaximum( 255 );
		intensity->SetWindowMinimum( (int) windowMin );
		intensity->SetWindowMaximum( (int) windowMax );


		typedef itk::ImageFileWriter< WriteImageType >  Writer2Type;

		Writer2Type::Pointer writer2 = Writer2Type::New();
		ostringstream jpegFilename;
		jpegFilename <<  scaleDicomFilename << "_w" << maxWidth << "_h" << maxHeight << "_window" << window << "_level" << level <<"_.jpg";

		writer2->SetFileName( jpegFilename.str());

		//rescaler->SetInput( readCachedDICOM->GetOutput() );
		intensity->SetInput( readCachedDICOM->GetOutput() );


		writer2->SetInput( intensity->GetOutput() );

		try
			{
			writer2->Update();
			//std::cout<< "cached image info \n";
			//gdcmImageIO->Print(std::cout);
			if (DEBUG){std::cout << "nComponents (cached 2) greyscale pixel" << nComponents << std::endl;}
			}
		catch (itk::ExceptionObject & e)
			{
			std::cerr << "exception in file writer " << std::endl;
			std::cerr << e.GetDescription() << std::endl;
			std::cerr << e.GetLocation() << std::endl;
			return EXIT_FAILURE;
			}

		std::cout <<"JPEG:" << jpegFilename.str() <<"\n";
		return EXIT_SUCCESS;
	}
	int writeRGBPixels(int window, int level, int maxWidth,int maxHeight,  char* scaleDicomFilename){
		int nComponents;
		//typedef unsigned char WritePixelType;




		RGBReaderType::Pointer readCachedDICOM = RGBReaderType::New();


		ImageIOType::Pointer gdcmImageIO = ImageIOType::New();


		readCachedDICOM->SetImageIO(gdcmImageIO );
		readCachedDICOM->SetFileName(scaleDicomFilename);
		nComponents = gdcmImageIO->GetNumberOfComponents();





		typedef itk::ImageFileWriter< RGBWriteImageType >  RGBWriter2Type;

		RGBWriter2Type::Pointer writer2 = RGBWriter2Type::New();
		ostringstream jpegFilename;
		jpegFilename <<  scaleDicomFilename << "_w" << maxWidth << "_h" << maxHeight <<"_.jpg";

		writer2->SetFileName( jpegFilename.str());


		writer2->SetInput( readCachedDICOM->GetOutput() );

		try
			{
			writer2->Update();
			//gdcmImageIO->Print(std::cout);
			if (DEBUG) {std::cout << "nComponents (cached 2) rgbpixel" << nComponents << std::endl;}
			}
		catch (itk::ExceptionObject & e)
			{
			std::cerr << "exception in file writer " << std::endl;
			std::cerr << e.GetDescription() << std::endl;
			std::cerr << e.GetLocation() << std::endl;
			return EXIT_FAILURE;
			}

		std::cout <<"JPEG:" << jpegFilename.str() <<"\n";
		return EXIT_SUCCESS;
	}
	int generateRGBCacheFile(string inputDICOMFilename, char *scaleDicomFilename, int nComponents,
	double scale, AffineTransformType::Pointer affineTransform,
	InputImageType::SizeType size, RGBRegionFilterType::Pointer subRegion){
		typedef RGBImageType::IndexType                   RGBImageIndexType;
		typedef RGBImageType::Pointer                     RGBImagePointerType;
		typedef RGBImageType::RegionType                  RGBImageRegionType;
		typedef RGBImageType::SizeType                    RGBImageSizeType;
		typedef itk::VectorResampleImageFilter<RGBImageType, RGBImageType >  RGBFilterType;
		if (DEBUG){std::cout<<"reading from file " << inputDICOMFilename <<"\n";}
		RGBReaderType::Pointer reader = RGBReaderType::New();
		reader->SetFileName(inputDICOMFilename);
		reader->Update();
		RGBImageType::ConstPointer image = reader->GetOutput();
		if (DEBUG){std:cout << "creating scaled image with nComponents=" << nComponents << "\n";}
		double origin[ Dimension ];
		InputImageType::PointType   dicomOrigin  = image->GetOrigin();
		InputImageType::SpacingType   dicomSpacing  = image->GetSpacing();


		origin[0] = dicomOrigin[0]/scale;
		origin[1] = dicomOrigin[1]/scale;
std::cerr<< "Input origin = " <<dicomOrigin[0] <<","<<dicomOrigin[1]<<"\n";

		double spacing[ Dimension ];
		spacing[0] = dicomSpacing[0] ;//* scale;
		spacing[1] = dicomSpacing[1] ;//* scale;
std::cerr<< "Spacing  = " <<spacing[0] <<","<<spacing[1]<<"\n";

		RGBFilterType::Pointer filter = RGBFilterType::New();
		//ResampleFilterType::Pointer resampler = ResampleFilterType::New();
		//resampler->SetOutputOrigin( origin );

		typedef itk::VectorLinearInterpolateImageFunction<
									 RGBImageType, double >  RGBInterpolatorType;

		RGBInterpolatorType::Pointer interpolator = RGBInterpolatorType::New();

		filter->SetInterpolator( interpolator );
		if (subRegion){
				subRegion->SetInput(reader->GetOutput());
				filter->SetInput(subRegion->GetOutput());
				RGBImageType::IndexType subRegionStart;
				RGBImageType::RegionType subReg = subRegion->GetRegionOfInterest();
				subRegionStart = subReg.GetIndex();
				origin[0] = (dicomOrigin[0] + subRegionStart[0] * spacing[0] )/scale;
				origin[1] = (dicomOrigin[1] + subRegionStart[1] * spacing[1])/scale;
				filter->SetOutputOrigin( origin );
			}
			else{
				filter->SetInput(reader->GetOutput());
				filter->SetOutputOrigin( origin );
			}


		typedef itk::IdentityTransform< double, Dimension >  TransformType;
		TransformType::Pointer transform = TransformType::New();

		filter->SetTransform( transform );

		RGBInterpolatorType::Pointer rgbInterpolator = RGBInterpolatorType::New();
		//RGBResampleFilterType::Pointer resampler = RGBResampleFilterType::New();
		filter->SetOutputOrigin( origin );
		rgbInterpolator->SetInputImage(reader->GetOutput());
		filter->SetInterpolator( rgbInterpolator );
		filter->SetInput(reader->GetOutput());
		filter->SetTransform( affineTransform );
		filter->SetSize(size);
		//resampler->SetDefaultPixelValue(0);
		filter->SetOutputSpacing(spacing);
		typedef itk::ImageFileWriter< RGBImageType >  Writer1Type;
		Writer1Type::Pointer writer1 = Writer1Type::New();
		writer1->SetFileName( scaleDicomFilename );
		writer1->SetInput( filter->GetOutput() );
		ImageIOType::Pointer gdcmImageIO = ImageIOType::New();


		writer1->SetImageIO( gdcmImageIO );


		writer1->Update();
		return EXIT_SUCCESS;
	}
	int generateGrayscaleCachefile(ReaderType::Pointer reader, char *scaleDicomFilename,
		int nComponents,  double scale, AffineTransformType::Pointer affineTransform,
		InputImageType::SizeType size,GrayscaleRegionFilterType::Pointer subRegion){
		ImageType::ConstPointer image = reader->GetOutput();

		ImageIOType::Pointer gdcmImageIOComp1 = ImageIOType::New();
			double origin[ Dimension ];
			InputImageType::PointType   dicomOrigin  = image->GetOrigin();
			InputImageType::SpacingType   dicomSpacing  = image->GetSpacing();


std::cerr<< "Input origin = " <<dicomOrigin[0] <<","<<dicomOrigin[1]<<"\n";
			origin[0] = dicomOrigin[0]/scale;
			origin[1] = dicomOrigin[1]/scale;


			double spacing[ Dimension ];
			spacing[0] = dicomSpacing[0] ;//* scale;
			spacing[1] = dicomSpacing[1] ;//* scale;

			std::cerr<< "Spacing  = " <<spacing[0] <<","<<spacing[1]<<"\n";
			ResampleFilterType::Pointer resampler = ResampleFilterType::New();
			resampler->SetOutputOrigin( origin );
			InterpolatorType::Pointer interpolator = InterpolatorType::New();
			interpolator->SetInputImage(reader->GetOutput());
			resampler->SetInterpolator( interpolator );
			if (subRegion){
				subRegion->SetInput(reader->GetOutput());
				resampler->SetInput(subRegion->GetOutput());
				ImageType::IndexType subRegionStart;
				ImageType::RegionType subReg = subRegion->GetRegionOfInterest();
				subRegionStart = subReg.GetIndex();
				origin[0] = (dicomOrigin[0] + subRegionStart[0] * spacing[0] )/scale;
				origin[1] = (dicomOrigin[1] + subRegionStart[1] * spacing[1])/scale;
				resampler->SetOutputOrigin( origin );
			}
			else{
				resampler->SetInput(reader->GetOutput());
				resampler->SetOutputOrigin( origin );
			}
			std::cerr<< "Transformed Origin = " <<origin[0] <<","<<origin[1]<<"\n";
			resampler->SetTransform( affineTransform );
			resampler->SetSize(size);
			//resampler->SetDefaultPixelValue(0);
			resampler->SetOutputSpacing(spacing);
			typedef itk::ImageFileWriter< InputImageType >  Writer1Type;
			Writer1Type::Pointer writer1 = Writer1Type::New();
			writer1->SetFileName( scaleDicomFilename );
			writer1->SetInput( resampler->GetOutput() );



			gdcmImageIOComp1->SetNumberOfComponents(nComponents);

			writer1->SetImageIO( gdcmImageIOComp1 );



			writer1->Update();
			return EXIT_SUCCESS;
	}

	int main(int ac, char* av[]){
		int result;
		for (int i=0;i<1;i++){
			result = windowLevelCache(ac, av, i);
		}
		return(result);
	}
