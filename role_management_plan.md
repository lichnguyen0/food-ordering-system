# Kế hoạch triển khai tính năng Role Management (Quản lý phân quyền)

Tính năng Role Management giúp Admin cấp quyền hạn cụ thể cho từng nhóm người dùng (ví dụ: Admin, Nhân viên kho, Nhân sự, Chăm sóc khách hàng,...). Dưới đây là kế hoạch chi tiết từng bước để tích hợp vào hệ thống hiện tại:

## 1. Thiết kế Cơ sở dữ liệu (Database Schema)
Cần bổ sung các bảng sau (nếu chưa có hoặc cần mở rộng):
*   **Bảng `roles`**: Lưu thông tin vai trò.
    *   `id` (PK)
    *   `name` (Tên hiển thị: Admin, Staff, Shipper...)
    *   `code` (Mã hệ thống: ROLE_ADMIN, ROLE_STAFF...)
    *   `description` (Mô tả)
*   **Bảng `permissions`**: Lưu danh sách các quyền hạn cụ thể trong hệ thống.
    *   `id` (PK)
    *   `name` (Tên hiển thị: Quản lý món ăn, Duyệt đơn hàng...)
    *   `code` (Mã hệ thống: MANAGE_FOOD, APPROVE_ORDER...)
*   **Bảng `role_permissions`**: Bảng trung gian n-n giữa `roles` và `permissions`.
    *   `role_id` (FK)
    *   `permission_id` (FK)
*   **Bảng `user_roles`**: Bảng trung gian n-n giữa `users` và `roles`.

## 2. Xây dựng Backend (Spring Boot)
*   **Entities:** Tạo các class `Role.java`, `Permission.java` với cấu hình JPA mapping `@ManyToMany` tương ứng.
*   **Repositories:** Tạo `RoleRepository` và `PermissionRepository`.
*   **DTOs:** Tạo `RoleDTO`, `RoleRequest` (để nhận dữ liệu từ form, bao gồm danh sách ID của các permissions).
*   **Services (`RoleService`, `RoleServiceImpl`):**
    *   Lấy danh sách tất cả các Role.
    *   Tạo mới/Cập nhật/Xóa Role (ràng buộc không cho xóa các Role hệ thống mặc định).
*   **Controllers (`AdminRoleController`):**
    *   `GET /admin/roles`: Trả về giao diện danh sách Role.
    *   `GET /admin/roles/create` & `GET /admin/roles/edit/{id}`: Trả về form.
    *   `POST /admin/roles/save` & `POST /admin/roles/delete/{id}`: Xử lý lưu/xóa.

## 3. Xây dựng Frontend (Giao diện Thymeleaf)
*   **Cập nhật `admin-layout.html`:** Thêm menu "Quản lý vai trò" vào thanh sidebar (Có thể bỏ comment phần Users/Settings).
*   **Trang `roles-list.html`:** Bảng hiển thị danh sách các Role.
*   **Trang `role-form.html`:** Input thông tin vai trò & Checkbox Group chọn các `permissions` (được nhóm theo module).

## 4. Cấu hình Spring Security
*   **`CustomUserDetailsService`:** Load Permissions của User gán vào `GrantedAuthority`.
*   **`SecurityConfig`:** Áp dụng phân quyền `.hasAuthority("...")` cho các route.

## 5. Quy trình làm việc (Đề xuất)
1.  Kiểm tra code hiện tại (Entities `User`, `Role` và `SecurityConfig`).
2.  Tạo Database Schema và các Entity/Repository cho `Permission`.
3.  Viết giao diện và Controller/Service cho trang Role.
4.  Áp dụng phân quyền mới vào Security.
