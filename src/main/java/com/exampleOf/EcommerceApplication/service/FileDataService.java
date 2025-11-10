package com.exampleOf.EcommerceApplication.service;

import com.exampleOf.EcommerceApplication.Exception.CustomException.OperationFailedException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ResourceNotFoundException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.UnauthorizedAccessException;
import com.exampleOf.EcommerceApplication.entity.FileData;
import com.exampleOf.EcommerceApplication.entity.Product;
import com.exampleOf.EcommerceApplication.repository.FileDataRepo;
import com.exampleOf.EcommerceApplication.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileDataService {
    private final FileDataRepo fileDataRepo;
    private final ProductRepo productRepo;

    // -------------------- Remove Image from Product -------------------- //
    @Transactional
    public void removeImageFromProduct(Long productId, Long fileDataId) {
        try {
            // Verify product exists
            if (!productRepo.existsById(productId)) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }

            // Find the file data
            FileData fileData = fileDataRepo.findById(fileDataId)
                    .orElseThrow(() -> new ResourceNotFoundException("FileData", "id", fileDataId));

            // Verify the image belongs to the specified product
            if (fileData.getProduct() == null || !fileData.getProduct().getId().equals(productId)) {
                throw new UnauthorizedAccessException("remove this image from the specified product");
            }

            // Check if we're trying to delete the primary image
            if (Boolean.TRUE.equals(fileData.getIsPrimary())) {
                // Find another image to set as primary if available
                List<FileData> otherImages = fileDataRepo.findByProductIdAndIsActiveTrue(productId);
                otherImages.remove(fileData); // Remove the one we're deleting

                if (!otherImages.isEmpty()) {
                    // Set the first available image as primary
                    FileData newPrimary = otherImages.get(0);
                    newPrimary.setIsPrimary(true);
                    fileDataRepo.save(newPrimary);
                }
            }

            // Soft delete the file data (assuming you have isActive field in Base class)
            fileData.setIsActive(false);
            fileDataRepo.save(fileData);

            // Alternatively, if you want hard delete:
            // fileDataRepo.delete(fileData);

        } catch (ResourceNotFoundException | UnauthorizedAccessException ex) {
            throw ex; // Re-throw specific exceptions
        } catch (Exception ex) {
            throw new OperationFailedException("Remove image from product", ex.getMessage());
        }
    }

    // -------------------- Set Primary Image -------------------- //
    @Transactional
    public FileData setPrimaryImage(Long productId, Long fileDataId) {
        try {
            // Verify product exists
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

            // Find the file data to set as primary
            FileData newPrimaryImage = fileDataRepo.findById(fileDataId)
                    .orElseThrow(() -> new ResourceNotFoundException("FileData", "id", fileDataId));

            // Verify the image belongs to the specified product
            if (newPrimaryImage.getProduct() == null || !newPrimaryImage.getProduct().getId().equals(productId)) {
                throw new UnauthorizedAccessException("set this image as primary for the specified product");
            }

            // Reset primary flag for all other images of this product
            List<FileData> productImages = fileDataRepo.findByProductIdAndIsActiveTrue(productId);

            for (FileData image : productImages) {
                if (Boolean.TRUE.equals(image.getIsPrimary())) {
                    image.setIsPrimary(false);
                    fileDataRepo.save(image);
                }
            }

            // Set the new primary image
            newPrimaryImage.setIsPrimary(true);
            FileData updatedPrimary = fileDataRepo.save(newPrimaryImage);

            return updatedPrimary;

        } catch (ResourceNotFoundException | UnauthorizedAccessException ex) {
            throw ex; // Re-throw specific exceptions
        } catch (Exception ex) {
            throw new OperationFailedException("Set primary image", ex.getMessage());
        }
    }

    // -------------------- Get Product Images -------------------- //
    public List<FileData> getProductImages(Long productId) {
        try {
            // Verify product exists
            if (!productRepo.existsById(productId)) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }

            return fileDataRepo.findByProductIdAndIsActiveTrue(productId);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OperationFailedException("Get product images", ex.getMessage());
        }
    }

    // -------------------- Add Image to Product -------------------- //
    @Transactional
    public FileData addImageToProduct(Long productId, FileData fileData) {
        try {
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

            fileData.setProduct(product);

            // If this is the first image, set it as primary automatically
            List<FileData> existingImages = fileDataRepo.findByProductIdAndIsActiveTrue(productId);
            if (existingImages.isEmpty()) {
                fileData.setIsPrimary(true);
            }

            return fileDataRepo.save(fileData);
        } catch (Exception ex) {
            throw new OperationFailedException("Add image to product", ex.getMessage());
        }
    }

    // -------------------- Get Primary Image -------------------- //
    public FileData getPrimaryImage(Long productId) {
        try {
            return fileDataRepo.findByProductIdAndIsPrimaryTrue(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Primary image for product", "id", productId));
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OperationFailedException("Get primary image", ex.getMessage());
        }
    }

    // -------------------- Reorder Images -------------------- //
    @Transactional
    public void reorderImages(Long productId, List<Long> imageIdsInOrder) {
        try {
            // Verify product exists
            if (!productRepo.existsById(productId)) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }

            List<FileData> productImages = fileDataRepo.findByProductIdAndIsActiveTrue(productId);

            // Update sort order based on the provided list order
            for (int i = 0; i < imageIdsInOrder.size(); i++) {
                Long imageId = imageIdsInOrder.get(i);
                FileData image = productImages.stream()
                        .filter(img -> img.getId().equals(imageId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("FileData", "id", imageId));

                image.setSortOrder(i);
                fileDataRepo.save(image);
            }
        } catch (Exception ex) {
            throw new OperationFailedException("Reorder images", ex.getMessage());
        }
    }

    // Add this method to FileDataService.java

    // -------------------- Upload Product Images -------------------- //
    @Transactional
    public List<FileData> uploadProductImages(
            Long productId,
            List<MultipartFile> files,
            List<String> altTexts,
            List<Integer> sortOrders,
            List<Boolean> isPrimary) throws IOException {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId,"",""));

        // Reset existing primary if new primary is being uploaded
        Boolean hasNewPrimary = isPrimary != null && isPrimary.contains(true);
        if (hasNewPrimary) {
            fileDataRepo.resetPrimaryImages(productId);
        }

        List<FileData> uploadedImages = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            if (file.isEmpty()) {
                throw new OperationFailedException("File at index " + i + " is empty","");
            }

            FileData fileData = saveFileToSystem(file, product, altTexts, sortOrders, isPrimary, i);
            uploadedImages.add(fileDataRepo.save(fileData));
        }

        return uploadedImages;
    }

    private FileData saveFileToSystem(MultipartFile file, Product product,
                                      List<String> altTexts, List<Integer> sortOrders,
                                      List<Boolean> isPrimary, int index) throws IOException {

        String originalFileName = file.getOriginalFilename();
        String fileName = generateUniqueFileName(originalFileName);
        String filePath = "uploads/products/" + fileName;

        // Create directory and save file
        Path uploadPath = Paths.get("uploads/products/");
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), Paths.get(filePath));

        boolean currentIsPrimary = getIsPrimary(isPrimary, index, product);
        int currentSortOrder = getSortOrder(sortOrders, index, product);

        return FileData.builder()
                .fileName(fileName)
                .filePath(filePath)
                .fileType(getFileExtension(originalFileName))
                .fileSize(file.getSize())
                .altText(getSafeListValue(altTexts, index, ""))
                .sortOrder(currentSortOrder)
                .isPrimary(currentIsPrimary)
                .mimeType(file.getContentType())
                .product(product)
                .build();
    }

    /**
     * Safely get value from list by index with fallback default value
     * Prevents IndexOutOfBoundsException and null pointer issues
     */
    private <T> T getSafeListValue(List<T> list, int index, T defaultValue) {
        if (list == null || list.isEmpty() || index < 0 || index >= list.size()) {
            return defaultValue;
        }

        T value = list.get(index);
        return value != null ? value : defaultValue;
    }

    private boolean getIsPrimary(List<Boolean> isPrimary, int index, Product p) {
        if (isPrimary == null || index >= isPrimary.size()) {
            // Default: first image is primary if no primary specified
            return index == 0 && !hasExistingPrimary(p);
        }
        return Boolean.TRUE.equals(isPrimary.get(index));
    }

    private int getSortOrder(List<Integer> sortOrders, int index, Product p) {
        if (sortOrders == null || index >= sortOrders.size()) {
            // Default: continue from existing max sort order
            Integer maxSortOrder = fileDataRepo.findMaxSortOrderByProductId(p.getId());
            return (maxSortOrder != null ? maxSortOrder + 1 : 0) + index;
        }
        return sortOrders.get(index);
    }

    private boolean hasExistingPrimary(Product product) {
        return fileDataRepo.existsByProductAndIsPrimary(product, true);
    }
//    @Transactional
//    public List<FileData> uploadProductImages(
//            Long productId,
//            List<MultipartFile> files,
//            List<String> altTexts,
//            List<Integer> sortOrders,
//            List<Boolean> isPrimary) {
//
//        try {
//            // Verify product exists
//            Product product = productRepo.findById(productId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
//
//            List<FileData> uploadedImages = new ArrayList<>();
//
//            for (int i = 0; i < files.size(); i++) {
//                MultipartFile file = files.get(i);
//
//                // Validate file
//                if (file.isEmpty()) {
//                    throw new OperationFailedException("Upload images", "File at index " + i + " is empty");
//                }
//
//                // Generate unique filename
//                String originalFileName = file.getOriginalFilename();
//                String fileExtension = getFileExtension(originalFileName);
//                String fileName = generateUniqueFileName(originalFileName);
//                String filePath = "uploads/products/" + fileName;
//
//                // Create uploads directory if it doesn't exist
//                File uploadDir = new File("uploads/products/");
//                if (!uploadDir.exists()) {
//                    uploadDir.mkdirs();
//                }
//
//                // Save file to filesystem
//                File destination = new File(filePath);
//                file.transferTo(destination);
//
//                // Create FileData entity
//                FileData fileData = FileData.builder()
//                        .fileName(fileName)
//                        .filePath(filePath)
//                        .fileType(file.getContentType())
//                        .fileSize(file.getSize())
//                        .altText(getAltText(altTexts, i))
//                        .sortOrder(getSortOrder(sortOrders, i))
//                        .isPrimary(getIsPrimary(isPrimary, i))
//                        .mimeType(file.getContentType())
//                        .product(product)
//                        .build();
//
//                // Save to database
//                FileData savedFileData = fileDataRepo.save(fileData);
//                uploadedImages.add(savedFileData);
//            }
//
//            return uploadedImages;
//
//        } catch (Exception ex) {
//            throw new OperationFailedException("Upload product images", ex.getMessage());
//        }
//    }

// -------------------- Helper Methods -------------------- //

    private String getFileExtension(String fileName) {
        return fileName != null ? fileName.substring(fileName.lastIndexOf(".")) : "";
    }

    private String generateUniqueFileName(String originalFileName) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String randomString = UUID.randomUUID().toString().substring(0, 8);
        return timeStamp + "_" + randomString + getFileExtension(originalFileName);
    }

    private String getAltText(List<String> altTexts, int index) {
        return altTexts != null && index < altTexts.size() ? altTexts.get(index) : "";
    }

//    private Integer getSortOrder(List<Integer> sortOrders, int index) {
//        return sortOrders != null && index < sortOrders.size() ? sortOrders.get(index) : index;
//    }
//
//    private Boolean getIsPrimary(List<Boolean> isPrimary, int index) {
//        return isPrimary != null && index < isPrimary.size() ? isPrimary.get(index) : (index == 0);
//    }
}