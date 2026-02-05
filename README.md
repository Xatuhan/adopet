# 🐾 Adopet

Adopet, Android platformu için geliştirilmiş, Firebase altyapısını kullanan bir mobil uygulamadır.  
Proje, modern Android geliştirme prensipleri doğrultusunda Kotlin diliyle yazılmış olup, kullanıcı verilerinin güvenli ve ölçeklenebilir şekilde yönetilmesini hedefler.

Bu uygulama, **kişisel gelişim, ders projeleri ve staj başvurularında portföy olarak kullanılmak** üzere tek geliştirici tarafından hayata geçirilmiştir.

---

## 🚀 Özellikler

- 📱 Android (Kotlin) tabanlı modern mobil uygulama
- 🔐 Firebase Firestore entegrasyonu
- 🔄 Gerçek zamanlı veri yönetimi
- 🧱 Modüler ve okunabilir proje yapısı
- ⚙️ Gradle (Kotlin DSL) ile yapılandırma
- 🔒 Firebase güvenlik kuralları desteği

---

## 🛠️ Kullanılan Teknolojiler

| Teknoloji | Açıklama |
|---------|---------|
| **Kotlin** | Android uygulama geliştirme |
| **Android SDK** | Mobil platform |
| **Firebase Firestore** | Bulut tabanlı NoSQL veritabanı |
| **Gradle (KTS)** | Build ve bağımlılık yönetimi |
| **Git / GitHub** | Versiyon kontrolü |

---

## 📂 Proje Yapısı

```text
Adopet/
│
├── app/                  # Android uygulama modülü
│   ├── src/              # Kaynak kodlar
│   ├── build.gradle.kts  # App seviyesinde Gradle yapılandırması
│
├── firestore.rules       # Firebase güvenlik kuralları
├── build.gradle.kts      # Proje genel yapılandırması
├── settings.gradle.kts   # Modül ayarları
└── gradle.properties
