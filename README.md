🐾 Adopet

Mobil Evcil Hayvan Sahiplendirme ve Takip Platformu

Adopet, evcil hayvan sahiplendirme süreçlerini mobil ortama taşıyan, Firebase tabanlı, çoklu API ve sensör entegrasyonuna sahip modern bir Android uygulamasıdır.
Uygulama, Kotlin dili ve güncel Android mimarisi kullanılarak geliştirilmiştir.

Bu proje tek geliştirici tarafından, ders ve staj odaklı kişisel bir portföy çalışması olarak hazırlanmıştır.

🧠 Uygulama Ne Yapar?

Adopet’in temel amacı:

Evcil hayvan sahiplendirme ilanlarının güvenli, filtrelenebilir ve konum tabanlı şekilde yönetilmesini sağlamak

Uygulama ağırlıklı olarak şu işlevlere odaklanır:

📋 Sahiplendirme ilanlarını listeleme ve filtreleme

❤️ İlanları favorilere ekleme

➕ Yeni ilan oluşturma (GPS destekli)

🗺️ İlan konumlarını harita üzerinde gösterme

☁️ Tüm verileri Firebase Cloud Firestore üzerinden CRUD işlemleriyle yönetme

Bu yapı sayesinde proje, istemci–bulut mimarisini, API yönetimini ve sensör entegrasyonunu pratik olarak göstermektedir.

🚀 Özellikler

📱 Kotlin ile geliştirilmiş Android uygulama

☁️ Firebase Cloud Firestore (NoSQL – CRUD)

🔌 2 adet Web API entegrasyonu

📍 GPS sensörü ile konum alma

🗺️ Google Harita entegrasyonu

🔄 Gerçek zamanlı veri yönetimi

📦 RecyclerView ile performanslı listeleme

🎨 Material Design tabanlı modern UI

🛠️ Kullanılan Teknolojiler
Bileşen	Teknoloji
Dil	Kotlin
Platform	Android
Veritabanı	Firebase Cloud Firestore
API	OpenWeatherMap, Google Maps / Geocoding
Ağ	Retrofit 2, Gson
Asenkron	Kotlin Coroutines
Sensör	GPS
UI	AndroidX, Material Design
Görsel Yönetimi	Glide / Picasso
🔌 API & Sensör Entegrasyonları

🌦️ OpenWeatherMap API
Kullanıcının bulunduğu konuma göre hava durumu verisi sağlar.

🗺️ Google Maps & Geocoding API
İlanlara ait konumların harita üzerinde görselleştirilmesini sağlar.

📍 GPS Sensörü
Yeni ilan oluşturulurken cihazdan anlık koordinat alınır.

📡 Tüm API çağrıları asenkron olarak gerçekleştirilir ve UI thread’i bloklanmaz.

🔄 Uygulama Akış Diyagramı (Flowchart)

Aşağıdaki diyagram, kullanıcının uygulamaya girişinden ilan detayına kadar olan temel algoritmik akışı göstermektedir:

flowchart TD
    A[Uygulama Başlatılır] --> B{Kullanıcı Giriş Yapmış mı?}
    B -- Evet --> C[MainActivity Yüklenir]
    B -- Hayır --> D[Login / Register]
    D --> C

    C --> E[Firestore'dan İlanları Çek]
    E --> F[RecyclerView ile Listele]

    F --> G{Filtre Seçildi mi?}
    G -- Evet --> H[Listeyi Filtrele]
    G -- Hayır --> I[Listeyi Göster]

    I --> J[İlan Seçildi]
    J --> K[DetailActivity]
    K --> L[Google Maps ile Konumu Göster]

    C --> M[İlan Ekle]
    M --> N[GPS ile Konum Al]
    N --> O[Firestore'a Kaydet]


Bu akış:

Yetkilendirme kontrolü

Veri okuma (READ)

Veri oluşturma (CREATE)

API ve sensör kullanımını

net şekilde göstermektedir.


📂 Proje Yapısı
Adopet/
│
├── app/
│   ├── src/
│   └── build.gradle.kts
│
├── firestore.rules
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties

⚙️ Kurulum
git clone https://github.com/kullanici-adi/adopet.git


Android Studio ile projeyi aç

Firebase Console üzerinden proje oluştur

google-services.json dosyasını app/ klasörüne ekle

Firestore’u aktif hale getir

Uygulamayı çalıştır

🎯 Proje Amacı

Bu proje ile amaçlanan:

Modern Android mimarisini öğrenmek

Firebase ile gerçek zamanlı veri yönetimini uygulamak

API ve sensör entegrasyonlarını pratikte göstermek

Staj ve akademik değerlendirmelerde güçlü bir portföy sunmak

🧭 Gelecek Geliştirmeler

🔔 Firebase Cloud Messaging (FCM)

🖼️ Firebase Storage ile görsel yükleme

📍 Gelişmiş konum bazlı arama

💬 Gerçek zamanlı mesajlaşma

👤 Geliştirici

Batuhan Gürsoy
Bilişim Sistemleri Mühendisliği Öğrencisi

📄 Lisans

Bu proje eğitim ve kişisel kullanım amaçlıdır.
